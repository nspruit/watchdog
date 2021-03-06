package nl.tudelft.watchdog.core.logic.document;


/**
 * Estimates the nature of a document into one of {@link DocumentType}.
 */
public class DocumentClassifier {
	/**
	 * Classifies the document type of the given document, by analyzing its
	 * filename and its contents.
	 */
	public static DocumentType classifyDocument(String fileName,
			String filePath, String fileContents) {
		fileName = fileName.toLowerCase();
		// Collapse multiple spaces in a row to one
		String preparedContents = fileContents.replaceAll("\\s+", " ");

		if (isJavaFile(fileName)) {
			if (containsJUnitImports(preparedContents)
					&& containsTestAnnotation(preparedContents)) {
				return DocumentType.TEST;
			}
			if (containsTestingFramework(preparedContents)) {
				return DocumentType.TEST_FRAMEWORK;
			}
			if (fileName.contains("test")) {
				return DocumentType.FILENAME_TEST;
			}
			if (filePath.contains("test")) {
				return DocumentType.PATHNAMME_TEST;
			}
			return DocumentType.PRODUCTION;
		}

		return DocumentType.UNDEFINED;
	}

	private static boolean containsTestingFramework(String preparedContents) {
		if (preparedContents.contains("import org.mockito")) {
			return true;
		} else if (preparedContents.contains("import org.powermock")
				|| preparedContents.contains("import static org.powermock")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return <code>true</code> if fileName ends in .java
	 */
	private static boolean isJavaFile(String fileName) {
		if (fileName.endsWith(".java")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return <code>true</code> if JUnit3 or 4 is being used,
	 *         <code>false</code> otherwise.
	 */
	private static boolean containsJUnitImports(String fileContents) {
		return containsJUnit4Imports(fileContents)
				| containsJunit3Import(fileContents);
	}

	/**
	 * @return <code>true</code> if there's an import for org.junit or
	 *         org.testng
	 */
	private static boolean containsJUnit4Imports(String fileContents) {
		if (fileContents.contains("import org.junit")
				|| fileContents.contains("import static org.junit")) {
			return true;
		} else if (fileContents.contains("import org.testng")
				|| fileContents.contains("import static org.testng")) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean containsJunit3Import(String fileContents) {
		if (fileContents.contains("import junit.framework")) {
			return true;
		}
		return false;
	}

	/**
	 * @return <code>true</code> if the editor contains (at least one) @Test
	 *         annotation.
	 */
	private static boolean containsTestAnnotation(String contents) {
		if (contents.contains("@Test")) {
			// Junit4
			return true;
		} else if (contents.contains("extends TestCase")) {
			// Junit3
			return true;
		} else {
			return false;
		}
	}

}
