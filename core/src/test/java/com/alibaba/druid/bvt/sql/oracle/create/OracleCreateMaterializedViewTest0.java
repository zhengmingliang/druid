/*
 * Copyright 1999-2017 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.druid.bvt.sql.oracle.create;

import com.alibaba.druid.sql.OracleTest;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleStatementParser;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;

import java.util.List;

public class OracleCreateMaterializedViewTest0 extends OracleTest {
    public void test_types() throws Exception {
        String sql = //
                "CREATE MATERIALIZED VIEW sales_summary AS\n" +
                        "  SELECT\n" +
                        "      seller_no,\n" +
                        "      invoice_date,\n" +
                        "      sum(invoice_amt) as sales_amt\n" +
                        "    FROM invoice\n" +
                        "    WHERE invoice_date < CURRENT_DATE\n" +
                        "    GROUP BY\n" +
                        "      seller_no,\n" +
                        "      invoice_date\n" +
                        "    ORDER BY\n" +
                        "      seller_no,\n" +
                        "      invoice_date;\n";

        OracleStatementParser parser = new OracleStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();
        SQLStatement stmt = statementList.get(0);
        print(statementList);

        assertEquals(1, statementList.size());

        assertEquals("CREATE MATERIALIZED VIEW sales_summary\n" +
                        "AS\n" +
                        "SELECT seller_no, invoice_date, sum(invoice_amt) AS sales_amt\n" +
                        "FROM invoice\n" +
                        "WHERE invoice_date < CURRENT_DATE\n" +
                        "GROUP BY seller_no, invoice_date\n" +
                        "ORDER BY seller_no, invoice_date;",//
                SQLUtils.toSQLString(stmt, JdbcConstants.ORACLE));

        OracleSchemaStatVisitor visitor = new OracleSchemaStatVisitor();
        stmt.accept(visitor);

        System.out.println("Tables : " + visitor.getTables());
        System.out.println("fields : " + visitor.getColumns());
        System.out.println("coditions : " + visitor.getConditions());
        System.out.println("relationships : " + visitor.getRelationships());
        System.out.println("orderBy : " + visitor.getOrderByColumns());

        assertEquals(1, visitor.getTables().size());

        assertEquals(3, visitor.getColumns().size());

        assertTrue(visitor.getColumns().contains(new TableStat.Column("invoice", "seller_no")));
    }
}
