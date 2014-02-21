package org.fraunhofer.plugins.hts.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fraunhofer.plugins.hts.db.service.HazardService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
 
import static com.google.common.base.Preconditions.*;
 
public final class HazardServlet extends HttpServlet
{
    private final HazardService hazardService;
 
    public HazardServlet(HazardService hazardService)
    {
        this.hazardService = checkNotNull(hazardService);
    }
 
    //TODO remove the HTML code, fix the form and use the servlet to input to the database
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
    	   final PrintWriter w = res.getWriter();
           w.write("<h1>Hazard test</h1>");
    
           // the form to post more TODOs
           w.write("<form method=\"post\">");
           w.write("<input type=\"text\" name=\"task\" size=\"25\"/>");
           w.write("<input type=\"text\" name=\"\" size=\"25\"/>");
           w.write("&nbsp;&nbsp;");
           w.write("<input type=\"submit\" name=\"submit\" value=\"Add\"/>");
           w.write("</form>");
    
           w.write("<ol>");
           
           w.close();
    }
 
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        final String title = req.getParameter("task");
        final String description = req.getParameter("task");
        hazardService.add(title, description, "preparer", "hazardNum", new Date(), new Date(), new Date());
     
        res.sendRedirect(req.getContextPath() + "/plugins/servlet/hazardservlet");
    }
}