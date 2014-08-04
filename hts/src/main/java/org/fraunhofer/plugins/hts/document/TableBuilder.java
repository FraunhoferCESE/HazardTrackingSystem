package org.fraunhofer.plugins.hts.document;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType;

public class TableBuilder {

	private int rows = 1, cols = 1;
	private int width = 1000;
	private XWPFBorderType innerHBorder = XWPFBorderType.SINGLE;

	public TableBuilder() {
	}

	public TableBuilder size(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		return this;
	}

	public XWPFTable createTable(XWPFDocument doc) {
		XWPFTable table = doc.createTable(rows, cols);
		table.setCellMargins(0, 0, 0, 0);
		table.setWidth(width);
		table.setInsideHBorder(innerHBorder, 1, 0, "000000");

		return table;
	}

	public TableBuilder setInnerHBorder(XWPFBorderType borderStyle) {
		this.innerHBorder = borderStyle;
		return this;
	}

	public TableBuilder setWidth(int width) {
		this.width = width;
		return this;
	}

}
