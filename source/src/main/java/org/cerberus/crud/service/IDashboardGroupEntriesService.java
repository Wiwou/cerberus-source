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
package org.cerberus.crud.service;

import java.util.List;
import org.cerberus.crud.entity.DashboardGroupEntries;
import org.cerberus.crud.entity.User;
import org.cerberus.dto.DashboardGroupEntriesDTO;

/**
 *
 * @author utilisateur
 */
public interface IDashboardGroupEntriesService {

    public List<DashboardGroupEntries> readByUser(User user);

    public List<DashboardGroupEntriesDTO> readDashboard(User user);
    
    public DashboardGroupEntriesDTO dashboardGroupEntriesToDTO(DashboardGroupEntries dashboardGroupEntries);

}
