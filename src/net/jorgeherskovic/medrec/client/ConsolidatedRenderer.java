package net.jorgeherskovic.medrec.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.jorgeherskovic.medrec.client.event.RedrawEvent;
import net.jorgeherskovic.medrec.client.event.RowDroppedEvent;
import net.jorgeherskovic.medrec.shared.Consolidation;
import net.jorgeherskovic.medrec.shared.Medication;
import net.jorgeherskovic.medrec.shared.ReconciledMedication;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HasVerticalAlignment;

public class ConsolidatedRenderer extends TableRenderer {
	private static String[] columnStyles = { "DragHandle", "EntryNumber",
			"Origin", "Medication", "Dosage", "Frequency", "Date", "Date",
			"Form", "Relation" };

	public ConsolidatedRenderer(DraggableFlexTable table, String[] headings,
			SimpleEventBus bus) {
		super(table, headings, bus);
	}

	private void makeCellDoubleHeight(int row, int col) {
		CellFormatter cf = this.getAttachedTable().getCellFormatter();
		// // DraggableFlexTable t=this.getAttachedTable();
		//
		// String cell_HTML = t.getHTML(row, col);
		// // AbsolutePanel new_panel=new AbsolutePanel();
		// // new_panel.setSize(this.getAttachedTable().getCellFormatter().get,
		// // height);
		// HTML HTML_control = new HTML(cell_HTML);
		// // new_panel.add();
		// t.setWidget(row, col, HTML_control);
		// int original_height = HTML_control.getOffsetHeight();
		// int original_width = HTML_control.getOffsetWidth();
		//
		// // TODO: use height to double the cell's height.
		// t.remove(HTML_control);
		// String new_height = Integer.toString(original_height * 2);
		// HTML_control.setHeight(new_height);
		//
		// AbsolutePanel new_panel=new AbsolutePanel();
		// new_panel.setSize(Integer.toString(original_width), new_height);
		// new_panel.add(HTML_control);
		//
		// t.setWidget(row, col, new_panel);
		// //
		// this.getAttachedTable().getCellFormatter().setVerticalAlignment(row,
		// // col, HasVerticalAlignment.ALIGN_BOTTOM);
		//
		// this.getAttachedTable().getCellFormatter()
		// .setHeight(row, col, new_height);

		// t.getFlexCellFormatter().setRowSpan(row, col, 2);
		cf.removeStyleName(row, col, "NoReconciliation");
		cf.removeStyleName(row, col, "PartialReconciliation");
		cf.addStyleName(row, col, "FullReconciliation");
	}

	private void flattenCell(int row, int col) {
		CellFormatter cf = this.getAttachedTable().getCellFormatter();
		// this.getAttachedTable().getCellFormatter().setHeight(row, col, "0");
		cf.removeStyleName(row, col, "NoReconciliation");
		cf.removeStyleName(row, col, "PartialReconciliation");
		cf.addStyleName(row, col, "FullReconciliation");
	}

	@Override
	public void renderTable() {
		DraggableFlexTable t = this.getAttachedTable();
		List<Consolidation> cons = t.getMedList();

		t.removeAllRows();
		Map<HTML, Consolidation> rowMapping = t.getRowMapping();
		rowMapping.clear();

		this.renderTableHeadings("TableHeading");
		int currentRow = 1;

		for (int i = 0; i < cons.size(); i++) {
			ReconciledMedication this_cons = new ReconciledMedication(
					cons.get(i), 0);
			Medication m1 = this_cons.getMed1();
			Medication m2 = this_cons.getMed2();
			int col = 0;

			/* Create an HTML widget and make it draggable */

			HTML handle = new HTML();
			handle.setHTML(this.dragToken);

			t.setWidget(currentRow, col++, handle);
			t.getRowDragController().makeDraggable(handle);

			rowMapping.put(handle, this_cons);

			t.setText(currentRow, col++, Integer.toString(i + 1));
			t.setHTML(currentRow, col++, m1.getProvenance());

			t.setHTML(currentRow, col++, m1.getMedicationName());
			String dosage1 = m1.getDose() + " " + m1.getUnits();
			t.setHTML(currentRow, col++, dosage1);
			t.setHTML(currentRow, col++, m1.getInstructions());
			t.setHTML(currentRow, col++, m1.getStartDateString());
			t.setHTML(currentRow, col++, m1.getEndDateString());
			t.setHTML(currentRow, col++, m1.getFormulation());
			t.setText(currentRow, col++, this_cons.getExplanation());

			String thisStyle = "NoReconciliation";
			if (this_cons.getScore() > 0.1) {
				thisStyle = "PartialReconciliation";
			}
			if (this_cons.getScore() > 0.99) {
				thisStyle = "FullReconciliation";
			}
			t.getRowFormatter().addStyleName(currentRow, thisStyle);

			String cellFormatStyle;

			if (this_cons.length == 1) {
				cellFormatStyle = "SingleRowDesign";
			} else {
				cellFormatStyle = "MultiRowTopDesign";
			}

			t.getRowFormatter().addStyleName(currentRow, cellFormatStyle);

			/*
			 * Apply the TableDesign style to each cell individually to get
			 * borders
			 */
			this.applyStyleToAllCellsInRow(currentRow, cellFormatStyle);
			this.applyStyleArrayToRow(currentRow, columnStyles);

			currentRow += 1;

			if (!m2.isEmpty()) {
				// Pre-build the current row so resizing works properly
				for (int j = 0; j < col; j++) {
					t.setHTML(currentRow, j, "&nbsp;");
				}

				col = 0;
				ArrayList<Integer> cells_to_squish = new ArrayList<Integer>();

				HTML second_handle = new HTML();
				second_handle.setHTML(this.dragToken);
				Consolidation reverse_cons = new ReconciledMedication(
						this_cons, 1);

				t.setWidget(currentRow, col++, second_handle);
				t.getRowDragController().makeDraggable(second_handle);

				t.setText(currentRow, col++, Integer.toString(i + 1));
				if (m2.getProvenance().equals(m1.getProvenance())) {
					// t.addCell(currentRow);
					// flattenCell(currentRow, col);
					// makeCellDoubleHeight(t, currentRow - 1, col++);
					cells_to_squish.add(col);
				}

				t.setHTML(currentRow, col++, m2.getProvenance());

				if (m2.getMedicationName().equals(m1.getMedicationName())) {
					// t.addCell(currentRow);
					// flattenCell(currentRow, col);
					// makeCellDoubleHeight(t, currentRow - 1, col++);
					cells_to_squish.add(col);
				}
				t.setHTML(currentRow, col++, m2.getMedicationName());

				String dosage2 = m2.getDose() + " " + m2.getUnits();
				if (dosage2.equals(dosage1)) {
					// t.addCell(currentRow);
					// flattenCell(currentRow, col);
					// makeCellDoubleHeight(t, currentRow - 1, col++);
					cells_to_squish.add(col);
				}
				t.setHTML(currentRow, col++, dosage2);

				if (m2.getInstructions().equals(m1.getInstructions())) {
					// t.addCell(currentRow);
					// flattenCell(currentRow, col);
					// makeCellDoubleHeight(t, currentRow - 1, col++);
					cells_to_squish.add(col);
				}
				t.setHTML(currentRow, col++, m2.getInstructions());

				if (m2.getStartDateString().equals(m1.getStartDateString())) {
					// t.addCell(currentRow);
					// flattenCell(currentRow, col);
					// makeCellDoubleHeight(t, currentRow - 1, col++);
					cells_to_squish.add(col);
				}
				t.setHTML(currentRow, col++, m2.getStartDateString());

				if (m2.getEndDateString().equals(m1.getEndDateString())) {
					// t.addCell(currentRow);
					// flattenCell(currentRow, col);
					// makeCellDoubleHeight(t, currentRow - 1, col++);
					cells_to_squish.add(col);
				}
				t.setHTML(currentRow, col++, m2.getEndDateString());

				if (m2.getFormulation().equals(m1.getFormulation())) {
					// t.addCell(currentRow);
					// flattenCell(currentRow, col);
					// makeCellDoubleHeight(t, currentRow - 1, col++);
					cells_to_squish.add(col);
				}
				t.setHTML(currentRow, col++, m2.getFormulation());

				cells_to_squish.add(col);
				t.setText(currentRow, col++, this_cons.getExplanation());
				// t.addCell(currentRow);
				// flattenCell(currentRow, col);
				// makeCellDoubleHeight(t, currentRow - 1, col++); // Make
				// explanation
				// double height

				t.getRowFormatter().addStyleName(currentRow,
						"MultiRowBottomDesign");
				t.getRowFormatter().addStyleName(currentRow, thisStyle);

				rowMapping.put(second_handle, reverse_cons);

				/*
				 * Apply the TableDesign style to each cell individually to get
				 * borders
				 */
				this.applyStyleToAllCellsInRow(currentRow,
						"MultiRowBottomDesign");
				this.applyStyleArrayToRow(currentRow, columnStyles);

				for (int j = 0; j < cells_to_squish.size(); j++) {
					flattenCell(currentRow, cells_to_squish.get(j));
					makeCellDoubleHeight(currentRow - 1, cells_to_squish.get(j));
				}

				currentRow += 1;

			}
		}

		return;

	}

	@Override
	public void handleDroppedRow(RowDroppedEvent event) {
		HTML handle = (HTML) event.getSourceTable().getWidget(
				event.getSourceRow(), 0);
		Consolidation cons = event.getSourceTable().getRowMapping().get(handle);
		Medication selected = cons.getSelectedMedication();
		if (selected == null) {
			selected = cons.getMed1();
		}
		// Window.alert("Dragged medication:" +
		// selected.getMedicationName()+" "+selected.getDose());
		// Someone has dropped a row on the Consolidated table. We need to add
		// *all* original rows to our internal representation.
		List<? extends Consolidation> myMedList = this.getAttachedTable()
				.getMedList();
		// count until we find the real target position
		int targetPosition = event.getDestRow() - 1;
		int realTargetPosition = 0;
		int tableTargetPosition = 0;

		if (targetPosition > 0) {
			while (tableTargetPosition < targetPosition) {
				tableTargetPosition += myMedList.get(realTargetPosition).length;
				realTargetPosition++;
			}
		}

		this.getAttachedTable().insertMed(realTargetPosition, cons);
		event.getSourceTable().deleteMed(event.getSourceRow() - 1);
		bus.fireEvent(new RedrawEvent());
	}

}
