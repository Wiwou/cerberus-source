/* Cerberus Copyright (C) 2013 - 2017 cerberustesting
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

This file is part of Cerberus.

Cerberus is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Cerberus is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.*/

package org.cerberus.dto;

import java.util.List;
import javax.annotation.Nullable;
import org.cerberus.crud.entity.DashboardEntry;
import org.cerberus.crud.entity.User;

/**
 *
 * @author cDelage
 */


public class DashboardGroupEntriesDTO {
    
    private Integer id;
    private String codeGroupEntries;
    private List<DashboardEntry> dashboardEntries;
    private String sort;
    private String associateElement;

    public DashboardGroupEntriesDTO(Integer id, String codeGroupEntries, List<DashboardEntry> dashboardEntries, String sort,@Nullable String associateElement) {
        this.id = id;
        this.codeGroupEntries = codeGroupEntries;
        this.dashboardEntries = dashboardEntries;
        this.sort = sort;
        this.associateElement = associateElement;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodeGroupEntries() {
        return codeGroupEntries;
    }

    public void setCodeGroupEntries(String codeGroupEntries) {
        this.codeGroupEntries = codeGroupEntries;
    }

    public List<DashboardEntry> getDashboardEntries() {
        return dashboardEntries;
    }

    public void setDashboardEntries(List<DashboardEntry> dashboardEntries) {
        this.dashboardEntries = dashboardEntries;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getAssociateElement() {
        return associateElement;
    }

    public void setAssociateElement(String associateElement) {
        this.associateElement = associateElement;
    }
    
    

    
}
