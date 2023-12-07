package org.learningequality.sqlite.schema;

import android.os.Bundle;

public interface DatabaseTable {
    public static final String DATABASE_NAME = "DATABASE_NAME";
    public static final String TABLE_NAME = "TABLE_NAME";

    String getTableName();

    interface Column<T> {
        String getColumnName();
    }

    interface ColumnEnum<T> extends Column<T> {
        T name();

        default T getValue() {
            return this.name();
        }

        ColumnImpl<T> getColumn();

        default String getColumnName() {
            return getColumn().getColumnName();
        }
    }

    abstract class ColumnImpl<T> implements Column<T> {
        private final String columnName;

        public ColumnImpl(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return this.columnName;
        }
    }

    class StringColumn extends ColumnImpl<String> {
        public StringColumn(String columnName) {
            super(columnName);
        }

        public String getValue(Bundle bundle) {
            return bundle.getString(getColumnName());
        }
    }
}
