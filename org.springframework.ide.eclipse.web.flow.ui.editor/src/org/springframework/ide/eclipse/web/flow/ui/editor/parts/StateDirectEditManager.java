package org.springframework.ide.eclipse.web.flow.ui.editor.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Text;

public class StateDirectEditManager extends DirectEditManager {

    protected Label activityLabel;

    Font scaledFont;

    protected VerifyListener verifyListener;

    public StateDirectEditManager(GraphicalEditPart source, Class editorType,
            CellEditorLocator locator, Label label) {
        super(source, editorType, locator);
        activityLabel = label;
    }

    protected void bringDown() {
        Font disposeFont = scaledFont;
        scaledFont = null;
        super.bringDown();
        if (disposeFont != null)
            disposeFont.dispose();
    }

    protected void initCellEditor() {
        Text text = (Text) getCellEditor().getControl();
        verifyListener = new VerifyListener() {

            public void verifyText(VerifyEvent event) {
                Text text = (Text) getCellEditor().getControl();
                String oldText = text.getText();
                String leftText = oldText.substring(0, event.start);
                String rightText = oldText.substring(event.end, oldText
                        .length());
                GC gc = new GC(text);
                Point size = gc.textExtent(leftText + event.text + rightText);
                gc.dispose();
                if (size.x != 0)
                    size = text.computeSize(size.x, SWT.DEFAULT);
                getCellEditor().getControl().setSize(size.x, size.y);
            }
        };
        text.addVerifyListener(verifyListener);

        String initialLabelText = activityLabel.getText();
        getCellEditor().setValue(initialLabelText);
        IFigure figure = ((GraphicalEditPart) getEditPart()).getFigure();
        scaledFont = figure.getFont();
        FontData data = scaledFont.getFontData()[0];
        Dimension fontSize = new Dimension(0, data.getHeight());
        activityLabel.translateToAbsolute(fontSize);
        data.setHeight(fontSize.height);
        scaledFont = new Font(null, data);

        text.setFont(scaledFont);
        text.selectAll();
    }

    protected void unhookListeners() {
        super.unhookListeners();
        Text text = (Text) getCellEditor().getControl();
        text.removeVerifyListener(verifyListener);
        verifyListener = null;
    }

}