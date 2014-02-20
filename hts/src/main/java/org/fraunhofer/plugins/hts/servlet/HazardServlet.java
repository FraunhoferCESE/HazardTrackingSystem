package org.fraunhofer.plugins.hts.servlet;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.transaction.TransactionCallback;
 
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fraunhofer.plugins.hts.data.service.HazardService;
import org.fraunhofer.plugins.hts.db.Hazards;

import java.io.IOException;
import java.io.PrintWriter;
 
import static com.google.common.base.Preconditions.*;
 
public final class HazardServlet extends HttpServlet
{
    private final ActiveObjects ao;
 
    public HazardServlet(ActiveObjects ao)
    {
        this.ao = checkNotNull(ao);
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
           w.write("&nbsp;&nbsp;");
           w.write("<input type=\"submit\" name=\"submit\" value=\"Add\"/>");
           w.write("</form>");
    
           w.write("<ol>");
           
           //ao.executeInTransaction(new TransactionCallback<Void>() // (1)
        	//	    {
        	//	        @Override
        	//	        public Void doInTransaction()
        	//	        {
        	//	            for (Hazards hazard : ao.find(Hazards.class)) // (2)
        	//	            {
        	//	                w.printf("<li><%2$s> %s </%2$s></li>", hazard.getDescription());
        	//	            }
        	//	            return null;
        	//	        }
        	//	    });
           w.close();
    }
 
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        final String title = req.getParameter("task");
        ao.executeInTransaction(new TransactionCallback<Hazards>() // (1)
        {
            @Override
            public Hazards doInTransaction()
            {
                final Hazards hazard = ao.create(Hazards.class); // (2)
                //hazard.setDescription(description); // (3)
                hazard.setTitle(title);
                hazard.save(); // (4)
                return hazard;
            }
        });
     
        res.sendRedirect(req.getContextPath() + "/plugins/servlet/hazardservlet");
    }
}