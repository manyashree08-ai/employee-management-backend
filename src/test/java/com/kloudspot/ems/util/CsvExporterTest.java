package com.kloudspot.ems.util;

import com.kloudspot.ems.dto.EmployeeResponse;
import com.kloudspot.ems.model.EmploymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvExporterTest {

    @Test
    void toCsv_containsHeaderAndRows() {
        EmployeeResponse e = new EmployeeResponse(1L, "EMP0001", "Aarav Sharma", "aarav@example.com",
                "9876543210", "Engineering", "Engineer", LocalDate.of(2024, 7, 1),
                new BigDecimal("850000.00"), EmploymentStatus.ACTIVE);

        String csv = CsvExporter.toCsv(List.of(e));
        String[] lines = csv.split("\r\n");

        assertEquals(2, lines.length);
        assertTrue(lines[0].startsWith("Employee ID,Full Name,Email"));
        assertEquals("EMP0001,Aarav Sharma,aarav@example.com,9876543210,Engineering,Engineer,2024-07-01,850000.00,ACTIVE",
                lines[1]);
    }

    @Test
    void escape_quotesFieldsWithCommasAndQuotes() {
        assertEquals("\"Smith, John\"", CsvExporter.escape("Smith, John"));
        assertEquals("\"He said \"\"hi\"\"\"", CsvExporter.escape("He said \"hi\""));
    }

    @Test
    void escape_neutralisesFormulaInjection() {
        assertEquals("'=SUM(A1:A2)", CsvExporter.escape("=SUM(A1:A2)"));
        assertEquals("'@cmd", CsvExporter.escape("@cmd"));
    }

    @Test
    void escape_handlesNull() {
        assertEquals("", CsvExporter.escape(null));
    }
}
