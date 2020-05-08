package org.openlowcode.client.graphic.widget.table;

import java.util.function.Function;


public interface ObjectDataElementKeyExtractor<E extends Object,F extends Object> {
	public Function<E,F> fieldExtractor();
	public Function<F,String> keyExtractor();
	public Function<F,String> labelExtractor();
	
}
