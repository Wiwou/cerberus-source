/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.crud.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.cerberus.crud.entity.Label;

import org.cerberus.crud.entity.TestCaseLabel;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * Interface that defines the public methods to manage Label data on table
 * Insert, Delete, Update, Find
 *
 * @author bcivel
 */
public interface ITestCaseLabelDAO {

    /**
     *
     * @param id
     * @return
     */
    AnswerItem<TestCaseLabel> readByKey(Integer id);

    /**
     *
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList<List<TestCaseLabel>> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     *
     * @param object
     * @return
     */
    Answer create(TestCaseLabel object);

    /**
     *
     * @param object
     * @return
     */
    Answer delete(TestCaseLabel object);

    /**
     *
     * @param object
     * @return
     */
    Answer update(TestCaseLabel object);

   
    /**
     * Uses data of ResultSet to create object {@link TestCaseLabel}
     *
     * @param rs ResultSet relative to select from table TestCaseLabel
     * @param label
     * @return object {@link TestCaseLabel}
     * @throws SQLException when trying to get value from
     * {@link java.sql.ResultSet#getString(String)}
     * @see FactoryTestCaseLabel
     */
    TestCaseLabel loadFromResultSet(ResultSet rs, Label label) throws SQLException;

    AnswerList readByTestTestCase(String test, String testCase);

}
