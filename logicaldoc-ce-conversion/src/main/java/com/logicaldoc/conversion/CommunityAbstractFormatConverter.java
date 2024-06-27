package com.logicaldoc.conversion;

import java.io.File;
import java.io.IOException;

import com.logicaldoc.core.conversion.AbstractFormatConverter;

/**
 * A base for community converters
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 7.7
 */
public abstract class CommunityAbstractFormatConverter extends AbstractFormatConverter {

	protected void checkFeature(File dest) throws IOException {
	}
}