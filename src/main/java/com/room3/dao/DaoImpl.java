package com.room3.dao;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

			for (int i = 0; i < columns.size(); i++) {
				if (i == 0) {
					insertCommand.append("?");
				} else {
					insertCommand.append(",?");
				}
			}
			insertCommand.append(");");

			String sql = insertCommand.toString();

			PreparedStatement stmt = con.prepareStatement(sql, new String[] { mta.getPrimaryKey().getColumnName() });
			System.out.println(stmt.toString());
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

	public <T> List<Object> findAll(Class<?> clazz) {

		Object b = Calculator.getNewInstance(clazz);

		try (Connection con = Configuration.getConnection()) {

			List<Object> objects = new ArrayList<Object>();
			MetaModel<Class<?>> mta = MetaModel.of(clazz);
			PrimaryKeyField pkField = mta.getPrimaryKey();
			List<ColumnField> columns = mta.getColumns();

			Field[] fields = clazz.getDeclaredFields();

			PreparedStatement stmt = con.prepareStatement("SELECT * FROM " + mta.getTableName());

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				for (Field field : fields) {

					field.setAccessible(true);
					
					try {
						if (field.getName().equals(pkField.getName())) {
							
							field.setInt(b, rs.getInt(pkField.getColumnName()));
							
						} else {

							for (ColumnField column : columns) {

								if (field.getName().equals(column.getName())) {

									switch (field.getType().getSimpleName()) {

									case "int":
										int jname = rs.getInt(column.getColumnName());
										field.set(b, jname);
										break;

									case "String":
										field.set(b, rs.getString(column.getColumnName()));
										break;
									}
								}

							}
						}
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				objects.add(b);
			}

			return objects;
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}

	public <T> Object selectById(Class<?> clazz, int id) {

		MetaModel<Class<?>> mta = MetaModel.of(clazz);

		PrimaryKeyField pk = mta.getPrimaryKey();
		List<ColumnField> columns = mta.getColumns();

		try (Connection con = Configuration.getConnection()) {
			Object b = Calculator.getNewInstance(clazz);

			StringBuilder sqlCommand = new StringBuilder(
					"SELECT * FROM " + mta.getTableName() + " WHERE " + pk.getColumnName() + " = " + id);

			String sql = sqlCommand.toString();

			PreparedStatement stmt = con.prepareStatement(sql);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				for (Field field : b.getClass().getDeclaredFields()) {

					field.setAccessible(true);

					if (field.getName() == pk.getName()) {
						b.getClass().getDeclaredField(pk.getName()).setInt(b, id);
					} else {

						for (ColumnField column : columns) {

							if (field.getName().equals(column.getName())) {
								field.set(b, rs.getString(column.getColumnName()));
							}

						}
					}
				}

			}
			return b;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public <T> void deleteById(Class<?> clazz, int id) {

		MetaModel<Class<?>> mta = MetaModel.of(clazz);

		try (Connection con = Configuration.getConnection()) {

			String sql = "DELETE FROM " + mta.getTableName() + " WHERE " + mta.getPrimaryKey().getColumnName() + " = "
					+ id;

			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void findAllClasses(String packageName) {
		Calculator cal = new Calculator();

		Reflections scan = new Reflections(packageName, new SubTypesScanner(false));
		Set<Class<?>> clazzes = scan.getSubTypesOf(Object.class).stream().collect(Collectors.toSet());
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

	public List<Object> findByValue(String value, String column, Class<?> o) {

		;

		MetaModel<Class<?>> mta = MetaModel.of(o);
		PrimaryKeyField pkField = mta.getPrimaryKey();
		List<ColumnField> columns = mta.getColumns();
		String idname = pkField.getName();
		List<Object> stuff = new ArrayList<Object>();
		try (Connection con = Configuration.getConnection()) {
			Object b = Calculator.getNewInstance(o);
			String sql = "SELECT * FROM " + o.getSimpleName().toLowerCase() + " WHERE " + column + " = " + "'" + value
					+ "'";

			PreparedStatement stmt = con.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

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
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return stuff;

	}

	public Object save(Object o) {

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

				Object b = Calculator.getNewInstance(o.getClass());
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
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return o;

	}

}