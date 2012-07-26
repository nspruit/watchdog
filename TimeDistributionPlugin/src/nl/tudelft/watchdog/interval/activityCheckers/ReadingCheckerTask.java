package nl.tudelft.watchdog.interval.activityCheckers;

import java.util.TimerTask;

import nl.tudelft.watchdog.eclipseUIReader.Events.DocumentAttentionEvent;
import nl.tudelft.watchdog.eclipseUIReader.Events.DocumentNotifier;

import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.ITextEditor;

public class ReadingCheckerTask extends TimerTask {

	private final StyledText styledText;
	private boolean stillActive;
	private CaretListener caretListener;
	private PaintListener paintListener;
	private RunCallBack callback;
	private ITextEditor editor;
	
	public ReadingCheckerTask(ITextEditor editor, RunCallBack callback) {
		stillActive = true;		
		this.callback = callback;
		this.editor = editor;
		styledText = (StyledText) editor.getAdapter(Control.class);
		
		createListeners();
	}
	

	private void createListeners() {	
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				caretListener = new CaretListener() { //cursor place changes		
					@Override
					public void caretMoved(CaretEvent event) {
						stillActive = true;
						styledText.removeCaretListener(this); //listen just once to not get millions of events fired
					}
				};
				
				paintListener = new PaintListener() { //for redraws of the view, e.g. when scrolled		
					@Override
					public void paintControl(PaintEvent e) {
						stillActive = true;
						styledText.removePaintListener(this); //listen just once to not get millions of events fired
					}
				};
			}
		});
	}
	
	@Override
	public void run() {
		if(stillActive){
			stillActive = false;
			
			Display.getDefault().asyncExec(new Runnable() {						
				@Override
				public void run() {
					styledText.addCaretListener(caretListener);
					styledText.addPaintListener(paintListener);
				}
			});
			
		}else{
			this.cancel();
			removeListeners();
			listenForReactivation();
			callback.onInactive();
		}
	}
	
	private void removeListeners() {		
		Display.getDefault().asyncExec(new Runnable() {			
			@Override
			public void run() {
				styledText.removeCaretListener(caretListener);
				styledText.removePaintListener(paintListener);
			}
		});
	}
	
	public void listenForReactivation(){
		Display.getDefault().asyncExec(new Runnable() {			
			@Override
			public void run() {
				styledText.addCaretListener(new CaretListener() { //cursor place changes		
					@Override
					public void caretMoved(CaretEvent event) {
						DocumentNotifier.fireDocumentStartFocusEvent(new DocumentAttentionEvent(editor));
						styledText.removeCaretListener(this); //listen just once to not get millions of events fired
					}
				});
				
				styledText.addPaintListener(new PaintListener() { //for redraws of the view, e.g. when scrolled		
					@Override
					public void paintControl(PaintEvent e) {
						DocumentNotifier.fireDocumentStartFocusEvent(new DocumentAttentionEvent(editor));
						styledText.removePaintListener(this); //listen just once to not get millions of events fired
					}
				});
			}
		});
	}

}
