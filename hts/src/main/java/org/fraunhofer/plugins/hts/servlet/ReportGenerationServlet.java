package org.fraunhofer.plugins.hts.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xmlbeans.XmlException;
import org.fraunhofer.plugins.hts.document.HazardReportGenerator;
import org.fraunhofer.plugins.hts.model.Hazards;
import org.fraunhofer.plugins.hts.model.Review_Phases;
import org.fraunhofer.plugins.hts.model.Risk_Categories;
import org.fraunhofer.plugins.hts.model.Risk_Likelihoods;
import org.fraunhofer.plugins.hts.service.HazardCauseService;
import org.fraunhofer.plugins.hts.service.HazardControlService;
import org.fraunhofer.plugins.hts.service.HazardService;
import org.fraunhofer.plugins.hts.service.ReviewPhaseService;
import org.fraunhofer.plugins.hts.service.RiskCategoryService;
import org.fraunhofer.plugins.hts.service.RiskLikelihoodsService;
import org.fraunhofer.plugins.hts.service.TransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.google.common.collect.Lists;

public final class ReportGenerationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final HazardService hazardService;
	private final ReviewPhaseService reviewPhaseService;
	private final RiskCategoryService riskCategoryService;
	private final RiskLikelihoodsService riskLikelihoodsService;
	private final HazardCauseService causeService;
	private final HazardControlService controlService;
	private final TransferService transferService;

	Logger log = LoggerFactory.getLogger(ReportGenerationServlet.class);

	public ReportGenerationServlet(HazardService hazardService, ReviewPhaseService reviewPhaseService,
			RiskCategoryService riskCategoryService, RiskLikelihoodsService riskLikelihoodsService,
			HazardCauseService causeService, TransferService transferService, HazardControlService controlService) {
		this.hazardService = hazardService;
		this.reviewPhaseService = reviewPhaseService;
		this.riskCategoryService = riskCategoryService;
		this.riskLikelihoodsService = riskLikelihoodsService;
		this.causeService = causeService;
		this.transferService = transferService;
		this.controlService = controlService;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String hazardID = request.getParameter("hazardToDownload");
		Hazards currentHazard = hazardService.getHazardByID(hazardID);

		List<Hazards> hazardList = Lists.newArrayList(currentHazard);
		List<Review_Phases> reviewPhasesList = new ArrayList<Review_Phases>(reviewPhaseService.all());
		List<Risk_Categories> riskCategoriesList = new ArrayList<Risk_Categories>(riskCategoryService.all());
		List<Risk_Likelihoods> riskLikelihoodsList = new ArrayList<Risk_Likelihoods>(riskLikelihoodsService.all());

		HazardReportGenerator reportGenerator = new HazardReportGenerator(hazardService, causeService, transferService,
				ComponentAccessor.getProjectManager(), controlService);

		ServletOutputStream stream = null;
		try {
			List<byte[]> results = reportGenerator.createWordDocument(hazardList, reviewPhasesList, riskCategoriesList,
					riskLikelihoodsList, getClass().getResourceAsStream("/Template.docx"));

			stream = response.getOutputStream();
			response.setContentType("application/msword");
			String fileName = currentHazard.getHazardNumber() == null ? "Unnamed_Hazard" : currentHazard.getHazardNumber();
			response.addHeader("Content-Disposition", "attachment; filename=" + fileName + ".docx");
			stream.write(results.get(0));
		} catch (IOException | XmlException ioe) {
			throw new ServletException(ioe.getMessage());
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}