package org.fraunhofer.plugins.hts.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import org.fraunhofer.plugins.hts.service.CauseService;
import org.fraunhofer.plugins.hts.service.ControlService;
import org.fraunhofer.plugins.hts.service.HazardService;
import org.fraunhofer.plugins.hts.service.ReviewPhaseService;
import org.fraunhofer.plugins.hts.service.RiskCategoryService;
import org.fraunhofer.plugins.hts.service.RiskLikelihoodsService;
import org.fraunhofer.plugins.hts.service.TransferService;
import org.fraunhofer.plugins.hts.service.VerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.google.common.collect.Maps;

public final class ReportGenerationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final HazardService hazardService;
	private final ReviewPhaseService reviewPhaseService;
	private final RiskCategoryService riskCategoryService;
	private final RiskLikelihoodsService riskLikelihoodsService;
	private final CauseService causeService;
	private final ControlService controlService;
	private final TransferService transferService;

	Logger log = LoggerFactory.getLogger(ReportGenerationServlet.class);
	private VerificationService verificationService;

	public ReportGenerationServlet(HazardService hazardService, ReviewPhaseService reviewPhaseService,
			RiskCategoryService riskCategoryService, RiskLikelihoodsService riskLikelihoodsService,
			CauseService causeService, TransferService transferService, ControlService controlService,
			VerificationService verificationService) {
		this.hazardService = hazardService;
		this.reviewPhaseService = reviewPhaseService;
		this.riskCategoryService = riskCategoryService;
		this.riskLikelihoodsService = riskLikelihoodsService;
		this.causeService = causeService;
		this.transferService = transferService;
		this.controlService = controlService;
		this.verificationService = verificationService;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] parameterValues = request.getParameterValues("hazardToDownload");

		if (parameterValues == null || parameterValues.length == 0) {
			return;
		} else {
			List<Review_Phases> reviewPhasesList = new ArrayList<Review_Phases>(reviewPhaseService.all());
			List<Risk_Categories> riskCategoriesList = new ArrayList<Risk_Categories>(riskCategoryService.all());
			List<Risk_Likelihoods> riskLikelihoodsList = new ArrayList<Risk_Likelihoods>(riskLikelihoodsService.all());

			HazardReportGenerator reportGenerator = new HazardReportGenerator(hazardService, causeService,
					transferService, ComponentAccessor.getProjectManager(), controlService, verificationService);

			// Generate the Word document byte arrays for each hazard
			Map<String, byte[]> results = Maps.newHashMap();
			int unnamed = 1;
			for (String hazardId : parameterValues) {
				Hazards currentHazard = hazardService.getHazardById(parameterValues[0]);
				String fileName = currentHazard.getHazardNumber() == null ? "Unnamed_Hazard" + unnamed++
						: currentHazard.getHazardNumber();
				byte[] wordDocument = null;
				try (InputStream templateFile = getClass().getResourceAsStream("/Template.docx")) {
					wordDocument = reportGenerator.createWordDocument(currentHazard, reviewPhasesList,
							riskCategoriesList, riskLikelihoodsList, templateFile);
				} catch (XmlException e) {
					log.error("Unable to generate report for hazard: " + hazardId, e);
				}
				if (wordDocument != null)
					results.put(fileName, wordDocument);
			}

			// Write the response. If a single file, send as a word doc. If
			// multiple files, zip them and send the zip
			try (ServletOutputStream stream = response.getOutputStream()) {
				if (results.size() == 1) {
					response.setContentType("application/msword");
					String filename = results.keySet().iterator().next();
					response.addHeader("Content-Disposition", "attachment; filename=" + filename + ".docx");
					stream.write(results.get(filename));
				} else if (results.size() > 1) {
					try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
							ZipOutputStream zos = new ZipOutputStream(bos)) {
						response.setContentType("application/octet-stream");
						response.addHeader("Content-Disposition", "attachment; filename=hazard_documents.zip");

						for (String filename : results.keySet()) {
							zos.putNextEntry(new ZipEntry("hazards" + File.separator + filename + ".docx"));
							zos.write(results.get(filename));
							zos.closeEntry();
						}
						zos.flush();
						zos.close();
						byte[] byteArray = bos.toByteArray();
						response.setContentLength(byteArray.length);
						stream.write(byteArray);
						response.flushBuffer();
						bos.close();
					}
				}
			} catch (IOException ioe) {
				throw new ServletException(ioe.getMessage());
			}
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}