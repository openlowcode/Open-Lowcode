package org.openlowcode.client.graphic.widget.table;

import java.util.function.BiConsumer;

import org.openlowcode.client.graphic.widget.fields.FormatValidator;
import org.openlowcode.client.graphic.widget.table.EditableTreeTable;

public interface ObjectDataElementValueUpdater<E extends Object,F extends Object>
		extends
		ObjectDataElementKeyExtractor<E,F> {
	public BiConsumer<E, F> payloadIntegration();
	public EditableTreeTable.Operator<F> operator();
	public FormatValidator<F> formatValidator();
	
}
