package com.kloudspot.ems.util;

import com.kloudspot.ems.dto.EmployeeResponse;

import java.util.List;

public final class CsvExporter {

    private static final String HEADER =
            "Employee ID,Full Name,Email,Phone Number,Department,Designation,Date of Joining,Salary,Status";

    private CsvExporter() {
    }

    public static String toCsv(List<EmployeeResponse> employees) {
        StringBuilder sb = new StringBuilder(HEADER).append("\r\n");
        for (EmployeeResponse e : employees) {
            sb.append(escape(e.employeeId())).append(',')
                    .append(escape(e.fullName())).append(',')
                    .append(escape(e.email())).append(',')
                    .append(escape(e.phoneNumber())).append(',')
                    .append(escape(e.department())).append(',')
                    .append(escape(e.designation())).append(',')
                    .append(escape(String.valueOf(e.dateOfJoining()))).append(',')
                    .append(escape(e.salary() == null ? "" : e.salary().toPlainString())).append(',')
                    .append(escape(String.valueOf(e.status())))
                    .append("\r\n");
        }
        return sb.toString();
    }

    /**
     * RFC 4180 escaping, plus neutralisation of spreadsheet formula injection
     * (values starting with = + - @ are prefixed with a single quote).
     */
    static String escape(String value) {
        if (value == null) {
            return "";
        }
        String v = value;
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
