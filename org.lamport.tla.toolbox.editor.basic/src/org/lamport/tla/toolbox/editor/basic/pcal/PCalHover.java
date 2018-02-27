package org.lamport.tla.toolbox.editor.basic.pcal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.lamport.tla.toolbox.editor.basic.util.DocumentHelper;
import org.lamport.tla.toolbox.editor.basic.util.DocumentHelper.WordRegion;

public class PCalHover implements ITextHover, IPCalReservedWords {
	
	private final Map<String, String> keywordHover = new HashMap<String, String>();

	public PCalHover() {
		keywordHover.put(ALGORITHM, ALGORITHM_HELP);
		keywordHover.put("algorithm", ALGORITHM_HELP);
		keywordHover.put(DEFINE, DEFINE_HELP);
		keywordHover.put(MACRO, MACRO_HELP);
		keywordHover.put(PROCEDURE, PROCEDURE_HELP);
		keywordHover.put(RETURN, RETURN_HELP);
		keywordHover.put(CALL, CALL_HELP);
		keywordHover.put(PROCESS, PROCESS_HELP);
		keywordHover.put(WHILE, WHILE_HELP);
		keywordHover.put("variable", VARIABLE_HELP);
		keywordHover.put("variables", VARIABLE_HELP);
		keywordHover.put(IF, IFTHENELSE_HELP);
		keywordHover.put(THEN, IFTHENELSE_HELP);
		keywordHover.put(ELSE, IFTHENELSE_HELP);
		keywordHover.put(ELSEIF, IFTHENELSE_HELP);
		keywordHover.put(EITHER, EITHEROR_HELP);
		keywordHover.put(OR, EITHEROR_HELP);
		keywordHover.put(WITH, WITH_HELP);
		keywordHover.put(AWAIT, AWAIT_HELP);
		keywordHover.put(PRINT, PRINT_HELP);
		keywordHover.put(ASSERT, ASSERT_HELP);
		keywordHover.put(SKIP, SKIP_HELP);
		keywordHover.put(GOTO, GOTO_HELP);
		keywordHover.put(":=", ASSIGN_HELP);
		keywordHover.put("||", MULTI_ASSIGN_HELP);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		// Expand the given region to word (token) and get the token. If it can't get
		// the token, no hover help is provided.
		final WordRegion wordRegion;
		try {
			wordRegion = DocumentHelper.getRegionExpandedBoth(textViewer.getDocument(),
					hoverRegion.getOffset(), new PCalWordDetector());
		} catch (BadLocationException ignore) {
			return null;
		}

		// Check if word matches any keywords.
		if (keywordHover.containsKey(wordRegion.getWord())) {
			return keywordHover.get(wordRegion.getWord());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return new Region(offset, 0);
	}
}