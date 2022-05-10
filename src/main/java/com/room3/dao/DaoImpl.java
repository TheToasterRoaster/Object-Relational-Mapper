package com.room3.dao;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.ConstructorUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import com.room3.annotations.Entity;
import com.room3.util.ColumnField;
import com.room3.util.Configuration;
import com.room3.util.MetaModel;
import com.room3.util.PrimaryKeyField;

public class DaoImpl {

	public <T> int insert(Object o) {

		MetaModel<Class<?>> mta = MetaModel.of(o.getClass());
		List<ColumnField> columns = mta.getColumns();

		try (Connection con = Configuration.getConnection()) {

			StringBuilder insertCommand = new StringBuilder("INSERT INTO " + mta.getTableName() + " (");

			for (ColumnField column : columns) {

				if (column == columns.get(columns.size() - 1)) {
					insertCommand.append(column.getColumnName() + ") VALUES (");
				} else {
					insertCommand.append(column.getColumnName() + ", ");
				}
			}

			for (int i = 1; i < columns.size(); i++) {
				if (i == 1) {
					insertCommand.append("?");
				} else {
					insertCommand.append(",?");
				}
			}
			insertCommand.append(");");

			String sql = insertCommand.toString();

			PreparedStatement stmt = con.prepareStatement(sql, new String[] { mta.getPrimaryKey().getColumnName() });

			int index = 1;

			for (ColumnField f : columns) {

				try {
					Field field = o.getClass().getDeclaredField(f.getName());
					field.setAccessible(true);

					String fieldType = f.getType().getSimpleName();

					switch (fieldType) {
					case "int":
						stmt.setInt(index, (int) field.get(o));
						index++;
						break;
					case "String":
						stmt.setString(index, (String) field.get(o));
						index++;
						break;
					case "boolean":
						stmt.setBoolean(index, (boolean) field.get(o));
						index++;
						break;
					case "double":
						stmt.setDouble(index, (double) field.get(o));
						index++;
						break;
					case "byte":
						stmt.setByte(index, (byte) field.get(o));
						index++;
						break;
					case "float":
						stmt.setFloat(index, (float) field.get(o));
						index++;
						break;
					case "long":
						stmt.setLong(index, (long) field.get(o));
						index++;
						break;
					case "short":
						stmt.setShort(index, (short) field.get(o));
						index++;
						break;

					}

				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
			int i = stmt.executeUpdate();

			if (i > 0) {

				ResultSet rs = stmt.getGeneratedKeys();
				while (rs.next()) {
					System.out.println(rs.getInt(1));
					return rs.getInt(1);
				}

			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public <T> List<Object> findAll(Object o) throws SQLException, NoSuchMethodException, SecurityException,
			IllegalAccessException, NoSuchFieldException {

		Class<?> clazz;

		Connection con = Configuration.getConnection();
		List<Object> flubber = new ArrayList<Object>();

		clazz = o.getClass();
		MetaModel<T> mta = new MetaModel<T>(clazz);
		PrimaryKeyField pkField = mta.getPrimaryKey();
		List<ColumnField> columns = mta.getColumns();
		Entity table = clazz.getDeclaredAnnotation(Entity.class);
		StringBuilder far = new StringBuilder();
		Field[] fields = clazz.getDeclaredFields();
		far.append("SELECT * FROM " + table.tableName().toLowerCase());
		String sql = far.toString();

		PreparedStatement stmt = con.prepareStatement(sql);
		ResultSet rs;
		if ((rs = stmt.executeQuery()) != null) {
			while (rs.next()) {
				Object b = createNewInstance(clazz.getName());

				for (Field f : fields) {
					String name = f.getName();
					f.setAccessible(true);
					String fieldType = null;
					String columnName = null;
					try {
						if (f.getName().equals(pkField.getName())) {

							int sname = rs.getInt(pkField.getColumnName());
							f.setInt(b, sname);
						} else {

							for (ColumnField c : columns) {

								if (f.getName().equals(c.getName())) {

									columnName = c.getColumnName();

									fieldType = c.getType().getSimpleName();

									switch (fieldType) {

									case "int":
										int jname = rs.getInt(columnName);
										f.set(b, jname);
										break;

									case "String":
										String uname = rs.getString(columnName);
										f.set(b, uname);
										break;
									}
								}

							}
						}
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				flubber.add(b);
			}
		}
		return flubber;
	}

	public <T> Object selectById(Object o, int id) throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, InstantiationException, NoSuchFieldException, SecurityException {
		Constructor<? extends Object> p = o.getClass().getConstructor();
		Class<?> clazz = o.getClass();
		MetaModel<T> mta = new MetaModel(clazz);
		// List<MetaModel<Class<?>>> meta = cfg.getMetaModels();
		PrimaryKeyField pkFields = mta.getPrimaryKey();
		List<ColumnField> columns = mta.getColumns();

		try (Connection con = Configuration.getConnection()) {

			StringBuilder sqlCommand = new StringBuilder("SELECT * FROM " + clazz.getSimpleName().toLowerCase()
					+ " where " + pkFields.getColumnName() + " = " + id);

			String sql = sqlCommand.toString();

			PreparedStatement stmt = con.prepareStatement(sql);

			ResultSet rs;
			if ((rs = stmt.executeQuery()) != null) {
				while (rs.next()) {

					Object b = p.newInstance();

					String idname = pkFields.getName();
					Field field = b.getClass().getDeclaredField(idname);
					field.setAccessible(true);
					field.setInt(b, id);

					for (ColumnField f : columns) {
						String name = f.getName();

						field = null;

						String fieldType = f.getType().getSimpleName();
						try {
							field = b.getClass().getDeclaredField(name);
							field.setAccessible(true);

							switch (fieldType) {

							case "int":
								int jname = rs.getInt(f.getName());
								field.set(b, jname);
								break;
							case "String":

								String uname = rs.getString(f.getColumnName());

								field.set(b, uname);
								break;
							}

							o = b;
//								case "oolean":
//									
//									break;
//								case "double":
//									
//									break;
//								case "byte":
//									
//									break;
//								case "float":
//									
//									break;
//								case "long":
//									
//									break;
//								case "short":
//									
//									break;
							//
//								}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			}

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return o;

	}

	private Object createNewInstance(String clazzName) {
		Class<?> beanClass = null;
		Object beanInstance = null;
		try {
			beanClass = getClass().getClassLoader().loadClass(clazzName);
			beanInstance = ConstructorUtils.invokeConstructor(beanClass, null);
		} catch (Exception e) {
			System.out.println("Error during creating class" + clazzName);
		}
		return beanInstance;
	}

	public <T> void deleteById(Object o, int id) {

		Class<?> clazz = o.getClass();
		MetaModel<Class<?>> mta = MetaModel.of(clazz);

		try (Connection con = Configuration.getConnection()) {

			String sql = "DELETE FROM " + mta.getTableName() + " WHERE " + mta.getPrimaryKey().getColumnName() + " = "
					+ mta.getPrimaryKey();

			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void findAllClasses(String packageName) {
		Calculator cal = new Calculator();

		Reflections scan = new Reflections(packageName, new SubTypesScanner(false));
		Set <Class<?>> clazzes = scan.getSubTypesOf(Object.class).stream().collect(Collectors.toSet());
		Configuration p = new Configuration();

		try (Connection conn = Configuration.getConnection()) {

			p.addAnnotatedClasses(clazzes);

			for (com.room3.util.MetaModel<Class<?>> metamodel : p.getMetaModels()) {
				
				StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS " + metamodel.getTableName() + " (");

				List<ColumnField> columns = metamodel.getColumns();

				sb.append(metamodel.getPrimaryKey().getColumnName() + " SERIAL PRIMARY KEY");

				for (ColumnField column : columns) {

					sb.append(", " + column.getColumnName() + " " + cal.getColType(column));
				}
				
				sb.append(")");				
			
				PreparedStatement stmt = conn.prepareStatement(sb.toString());
				
				stmt.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<Object> selectAllByValueInColumn(String value, String column, Class o) {

		MetaModel<Class> mta = new MetaModel(o);
		PrimaryKeyField pkField = mta.getPrimaryKey();
		List<ColumnField> columns = mta.getColumns();
		String idname = pkField.getName();
		List<Object> stuff = new ArrayList<Object>();
		try (Connection con = Configuration.getConnection()) {

			String sql = "SELECT * FROM " + o.getSimpleName().toLowerCase() + " WHERE " + column + " = " + "'" + value
					+ "'";

			PreparedStatement stmt = con.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				Object b = createNewInstance(o.getName());
				Field[] fields = b.getClass().getDeclaredFields();

				for (Field field : fields) {
					field.setAccessible(true);
					try {
						if (field.getName().equals(pkField.getName())) {
							field.set(b, rs.getInt(pkField.getColumnName()));
							int pg = rs.getInt(pkField.getColumnName());

						} else {
							for (ColumnField columnfield : columns) {
								if (field.getName().equals(columnfield.getName())) {
									if (field.getType().getSimpleName().equals("String")) {
										field.set(b, rs.getString(columnfield.getColumnName()));
									} else if (field.getType().getSimpleName().equals("int")) {
										field.set(b, rs.getInt(columnfield.getColumnName()));
									}

								}
							}
						}
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						System.out.println("log this 1");
					}
				}
				stuff.add(b);
			}
		} catch (SQLException e) {
			System.out.println("log this");
			e.printStackTrace();
		}

		return stuff;

	}

	public Object updateSingle(Object o) {

		MetaModel<?> mta = MetaModel.of(o.getClass());
		PrimaryKeyField pkField = mta.getPrimaryKey();
		List<ColumnField> columns = mta.getColumns();

		String update = "UPDATE " + o.getClass().getSimpleName().toLowerCase() + " SET ";
		StringBuilder sb = new StringBuilder();
		sb.append(update);
		String id = "", columnValue;
		try (Connection con = Configuration.getConnection()) {

			for (Field field : o.getClass().getDeclaredFields()) {
				field.setAccessible(true);

				try {
					if (field.getName().equals(pkField.getName())) {
						id = " WHERE " + pkField.getColumnName() + " = " + field.get(o);

					} else {
						for (int i = 0; i < columns.size(); i++) {

							if (field.getName().equals(columns.get(i).getName())) {
								if (i < columns.size() - 1) {
									columnValue = columns.get(i).getColumnName() + " = '" + field.get(o) + "', ";
									sb.append(columnValue);
								} else {
									columnValue = columns.get(i).getColumnName() + " = '" + field.get(o) + "'";
									sb.append(columnValue);
								}
							}
						}
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();

				}
			}
			sb.append(id + " RETURNING *");
			PreparedStatement stmt = con.prepareStatement(sb.toString());
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				Object b = createNewInstance(o.getClass().getName());
				Field[] fields = b.getClass().getDeclaredFields();

				for (Field field : fields) {
					field.setAccessible(true);
					try {
						if (field.getName().equals(pkField.getName())) {
							field.set(b, rs.getInt(pkField.getColumnName()));
							int pg = rs.getInt(pkField.getColumnName());

						} else {
							for (ColumnField columnfield : columns) {
								if (field.getName().equals(columnfield.getName())) {
									if (field.getType().getSimpleName().equals("String")) {
										field.set(b, rs.getString(columnfield.getColumnName()));
									} else if (field.getType().getSimpleName().equals("int")) {
										field.set(b, rs.getInt(columnfield.getColumnName()));
									}

								}
							}
						}
						o = b;
					} catch (IllegalAccessException e) {
						e.printStackTrace();

					}
				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return o;

	}

}