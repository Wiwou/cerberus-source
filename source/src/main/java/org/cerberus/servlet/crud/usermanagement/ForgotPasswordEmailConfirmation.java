/*
 * Cerberus  Copyright (C) 2013  vertigo17
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
package org.cerberus.servlet.crud.usermanagement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.User;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.IUserService;
import org.cerberus.crud.service.impl.ParameterService;
import org.cerberus.crud.service.impl.UserService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerItem;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
public class ForgotPasswordEmailConfirmation extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IUserService userService = appContext.getBean(UserService.class);
            IParameterService parameterService = appContext.getBean(ParameterService.class);
            String system = "";
            JSONObject jsonResponse = new JSONObject();

            
            StringBuffer jb = new StringBuffer();
            String line = null;
                BufferedReader reader = request.getReader();
                while ((line = reader.readLine()) != null) {
                    jb.append(line);
                }
            String[] parameters = jb.toString().split("&");
            JSONObject jo = new JSONObject();
            for (String parameter : parameters){
                String[] param = parameter.split("=");
                jo.put(param[0], param[1]);
            }
            
            String login = jo.getString("login");
            String confirmation = jo.getString("confirmationToken");

            
            /**
             * If email not found in database, send error message
             */
            AnswerItem ai = userService.readByKey(login);
            User user = (User) ai.getItem();

            if (user == null) {
                jsonResponse.put("messageType", "Error");
                jsonResponse.put("message", "Login submitted is unknown !");
                response.getWriter().print(jsonResponse);
                response.getWriter().flush();
                return;
            }

            /**
             * Check the token
             */
            if (!userService.verifyResetPasswordToken(user, confirmation)) {
                jsonResponse.put("messageType", "Error");
                jsonResponse.put("message", "Token submitted is invalid !");
                response.getWriter().print(jsonResponse);
                response.getWriter().flush();
                return;
            }

            /**
             * Build Response Message
             */
            jsonResponse.put("messageType", "Success");
            jsonResponse.put("message", "Please, define your new password.");
            response.getWriter().print(jsonResponse);
            response.getWriter().flush();
        } catch (JSONException ex) {
            Logger.getLogger(ForgotPasswordEmailConfirmation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
