package org.fraunhofer.plugins.hts.document;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

import com.google.common.base.Joiner;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

public class RiskChart {

	private final int nCols, nRows;
	private final Table<Integer, Integer, List<String>> chart;

	public int getnCols() {
		return nCols;
	}

	public int getnRows() {
		return nRows;
	}

	public Table<Integer, Integer, List<String>> getChart() {
		return chart;
	}

	/**
	 * Creates a new table-based risk chart.
	 * 
	 * @param rows
	 *            must be > 0
	 * @param columns
	 *            must be > 0
	 */
	public RiskChart(int rows, int columns) {
		checkArgument(rows > 0 && columns > 0, "Minimum risk chart size is 1x1");
		this.nCols = columns;
		this.nRows = rows;

		chart = HashBasedTable.create(rows, columns);
	}

	/**
	 * Adds a new string value to the list in the row,column location
	 * 
	 * @param row
	 * @param column
	 * @param val
	 */
	public void addToChart(int row, int column, String val) {
		checkNotNull(val);
		checkArgument(row > 0 && row <= nRows && column > 0 && column <= nCols,
				"Location out of bounds. Chart size: %s rows, %s columns", nRows, nCols);

		List<String> list = chart.get(row, column);
		if (list == null) {
			list = Lists.newArrayList(val);
			chart.put(row, column, list);
		} else
			list.add(val);
	}

	/**
	 * Removes the first matching instance of a string value from the list in
	 * the row,column location
	 * 
	 * @param row
	 * @param column
	 * @param val
	 *            the value to be removed. Cannot be <code>null</code>.
	 * @return <code>true</code> if the value was removed, <code>false</code>
	 *         otherwise
	 */
	public boolean removeFromChart(int row, int column, String val) {
		checkNotNull(val);
		checkArgument(row > 0 && row <= nRows && column > 0 && column <= nCols,
				"Location out of bounds. Chart size: %s rows, %s columns", nRows, nCols);

		List<String> list = chart.get(row, column);
		boolean removed = false;
		if (list != null)
			removed = list.remove(val);

		return removed;
	}

	private class MyRenderer extends DefaultTableCellRenderer {
//		@Override
//		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
//				boolean hasFocus, int row, int column) {
//			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//
//			if (column == 4)
//				c.setBackground(Color.red);
//			return c;
//		}

		private static final long serialVersionUID = 1L;
	}

	private Component createTable() {

		TableModel dataModel = new AbstractTableModel() {
			private static final long serialVersionUID = 1L;

			public int getRowCount() {
				return nRows;
			}

			public int getColumnCount() {
				return nCols;
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				List<String> cell = chart.get(rowIndex + 1, columnIndex + 1);
				return cell == null ? "" : Joiner.on('\n').join(cell);
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return List.class;
			}

		};

		JXTable table = new JXTable(dataModel);

		table.setDefaultRenderer(List.class, new MyRenderer());

		table.packAll();

		JFrame f = new JFrame();
		f.setUndecorated(true);
		f.getContentPane().add(table);
		f.setSize(table.getPreferredSize());
		f.setVisible(true);

		return f;
	}

	public void saveComponentAsJPEG(String filename) {
		Component myComponent = createTable();

		Dimension size = myComponent.getSize();
		BufferedImage myImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = myImage.createGraphics();

		myComponent.paint(g2);
		try {
			OutputStream out = new FileOutputStream(filename);
			ImageIO.write(myImage,"jpeg",out);
			out.close();
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			g2.dispose();
		}
	}

}
