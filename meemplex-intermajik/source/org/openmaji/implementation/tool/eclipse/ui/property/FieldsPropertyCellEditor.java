/*
 * @(#)FieldsPropertyCellEditor.java
 *
 * Copyright 2003 by EkoLiving Pty Ltd.  All Rights Reserved.
 *
 * This software is the proprietary information of EkoLiving Pty Ltd.
 * Use is subject to license terms.
 */

/* ToDo:
 * - None, yet.
 */
package org.openmaji.implementation.tool.eclipse.ui.property;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * @author mg
 * Created on 14/01/2003
 */
public class FieldsPropertyCellEditor extends DialogCellEditor {

	private Label label;
	//private String implClassName;
	//private String dialogMessage;

	protected FieldsPropertyCellEditor(Composite parent, String implClassName, String dialogMessage) {
		super(parent);
		//this.implClassName = implClassName;
		//this.dialogMessage = dialogMessage;
	}

	protected Control createContents(Composite cell) {
		label = new Label(cell, SWT.LEFT);
		label.setFont(cell.getFont());
		label.setBackground(cell.getBackground());
		return label;
	}

	protected Object openDialogBox(Control cellEditorWindow) {
//		ClassDescriptor classDescriptor = (ClassDescriptor) ClassList.getInstance().get(implClassName);
//
//		Collection values = (Collection) getValue();
//
//		/* values is a collection of strings.
//		 * Need to turn it back into a collection of 
//		 * FieldDescriptors for initial selection
//		 */
//
//		List initialSelections = new ArrayList();
//
//		for (Iterator i = values.iterator(); i.hasNext();) {
//			String fieldName = (String)i.next();
//			initialSelections.add(findField(classDescriptor, fieldName));
//		}
//
//		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
//		FieldSelectionDialog dialog = new FieldSelectionDialog(shell, classDescriptor);
//		dialog.setInitialElementSelections(initialSelections);
//
//		dialog.setMessage(dialogMessage);
//
//		if (dialog.open() == Window.OK) {
//			Object[] result = dialog.getResult();
//
//			if (result.length == 0)
//				return values;
//
//			ArrayList fieldsList = new ArrayList();
//
//			for (int i = 0; i < result.length; i++) {
//				fieldsList.add(((FieldDescriptor) result[i]).getName());
//			}
//
//			return fieldsList;
//		}
//
//		return values;
		return null;
	}

	protected void updateContents(Object value) {
		if (value != null)
			label.setText(value.toString());
		else
			label.setText("");
	}
/*
	private FieldDescriptor findField(ClassDescriptor classDescriptor, String fieldName) {
		Collection fields = classDescriptor.getFields();
		for (Iterator i = fields.iterator(); i.hasNext();) {
			FieldDescriptor fieldDescriptor = (FieldDescriptor) i.next();
			if (fieldDescriptor.getName().equals(fieldName))
				return fieldDescriptor;
		}
		return null;
	}
*/
}