package org.fraunhofer.plugins.hts.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.Risk_Categories;
import org.fraunhofer.plugins.hts.db.Risk_Likelihoods;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.ReviewPhaseService;
import org.fraunhofer.plugins.hts.db.service.RiskCategoryService;
import org.fraunhofer.plugins.hts.db.service.RiskLikelihoodsService;
import org.fraunhofer.plugins.hts.document.HazardReportGenerator;

import com.google.common.collect.Lists;

public final class ReportGenerationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final HazardReportGenerator hazardReportGenerator;
	private final HazardService hazardService;
	private final ReviewPhaseService reviewPhaseService;
	private final RiskCategoryService riskCategoryService;
	private final RiskLikelihoodsService riskLikelihoodsService;

	public ReportGenerationServlet(HazardReportGenerator hazardReportGenerator, HazardService hazardService, 
			ReviewPhaseService reviewPhaseService, RiskCategoryService riskCategoryService, 
			RiskLikelihoodsService riskLikelihoodsService) {
		this.hazardReportGenerator = hazardReportGenerator;
		this.hazardService = hazardService;
		this.reviewPhaseService = reviewPhaseService;
		this.riskCategoryService = riskCategoryService;
		this.riskLikelihoodsService = riskLikelihoodsService;
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		final String hazardID = request.getParameter("hazardToDownload");
		List<File> hazardFiles = setupWordDocuments(hazardID);
		
		
		//get the 'file' parameter
		
	    //String fileName = (String) request.getParameter("file");
		String fileName = "test.docx";
		
	    if (fileName == null || fileName.equals(""))
	      throw new ServletException(
	          "Invalid or non-existent file parameter in Report Generation Servlet servlet.");
	
	    // add the .doc suffix if it doesn't already exist
	    if (fileName.indexOf(".doc") == -1)
	      fileName = fileName + ".doc";
	
	    //String wordDir = getServletContext().getInitParameter("word-dir");
	    String wordDir = "C:\\Temp\\";
	    
	    if (wordDir == null || wordDir.equals(""))
	      throw new ServletException(
	          "Invalid or non-existent wordDir context-param.");
	    ServletOutputStream stream = null;
	    BufferedInputStream buf = null;
	    try {
	      stream = response.getOutputStream();
	      File doc = new File(wordDir + "/" + fileName);
	      response.setContentType("application/msword");
	      response.addHeader("Content-Disposition", "attachment; filename="
	          + fileName);
	      response.setContentLength((int) doc.length());
	      FileInputStream input = new FileInputStream(doc);
	      buf = new BufferedInputStream(input);
	      int readBytes = 0;
	      while ((readBytes = buf.read()) != -1)
	        stream.write(readBytes);
	    } catch (IOException ioe) {
	      throw new ServletException(ioe.getMessage());
	    } finally {
	      if (stream != null)
	        stream.close();
	      if (buf != null)
	        buf.close();
	    }
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	private List<File> setupWordDocuments(String hazardID) {
		Hazards currentHazard = hazardService.getHazardByID(hazardID);
		List<Hazards> hazardList = Lists.newArrayList(currentHazard);
		
		List<Review_Phases> reviewPhasesList = new ArrayList<Review_Phases>(reviewPhaseService.all());
		List<Risk_Categories> riskCategoriesList = new ArrayList<Risk_Categories>(riskCategoryService.all());
		List<Risk_Likelihoods> riskLikelihoodsList = new ArrayList<Risk_Likelihoods>(riskLikelihoodsService.all());
		// one more parameter left to initialize before function call
		
		return null;
	}
}