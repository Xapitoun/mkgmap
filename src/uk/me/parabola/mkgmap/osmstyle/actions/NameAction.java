/*
 * Copyright (C) 2008 Steve Ratcliffe
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 * 
 * Author: Steve Ratcliffe
 * Create date: 02-Dec-2008
 */
package uk.me.parabola.mkgmap.osmstyle.actions;

import uk.me.parabola.mkgmap.reader.osm.Element;

/**
 * Set the name on the given element.  The tags of the element may be
 * used in setting the name.
 *
 * We have a list of possible substitutions.
 *
 * @author Steve Ratcliffe
 */
public class NameAction extends ValueBuildedAction {

	/**
	 * search for the first matching name pattern and set the element name
	 * to it.
	 *
	 * If the element name is already set, then nothing is done.
	 *
	 * @param el The element on which the name may be set.
	 */
	public void perform(Element el) {
		if (el.getTag("mkgmap:label:1") != null)
			return;
		
		for (ValueBuilder vb : getValueBuilder()) {
			String s = vb.build(el, el);
			if (s != null) {
				el.addTag("mkgmap:label:1", s);
				break;
			}
		}
	}

	public String toString() {
		StringBuilder sb = new  StringBuilder();
		sb.append("name ");
		for (ValueBuilder vb : getValueBuilder()) {
			sb.append(vb);
			sb.append(" | ");
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}
}
