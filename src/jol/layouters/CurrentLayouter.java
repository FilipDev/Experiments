/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package jol.layouters;

import jol.datamodel.CurrentDataModel;
import jol.info.ClassData;
import jol.info.ClassLayout;
import jol.info.FieldData;
import jol.info.FieldLayout;
import jol.util.VMSupport;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The layouter getting the actual VM layout.
 *
 * @author Aleksey Shipilev
 */
public class CurrentLayouter implements Layouter {

	@Override
	public ClassLayout layout(ClassData data)
	{
		CurrentDataModel model = new CurrentDataModel();

		if (data.isArray())
		{
			// special case of arrays
			try
			{
				int base = VMSupport.U.arrayBaseOffset(Class.forName(data.arrayClass()));
				int scale = VMSupport.U.arrayIndexScale(Class.forName(data.arrayClass()));

				int instanceSize = VMSupport.align(base + (data.arrayLength()) * scale, 8);

				SortedSet<FieldLayout> result = new TreeSet<FieldLayout>();
				result.add(new FieldLayout(FieldData.create(data.arrayClass(), "length", "int"), model.headerSize(), model.sizeOf("int")));
				result.add(new FieldLayout(FieldData.create(data.arrayClass(), "<elements>", data.arrayComponentType()), base, scale * data.arrayLength()));
				return new ClassLayout(data, result, model.headerSize(), instanceSize, false);
			} catch (ClassNotFoundException e)
			{
				throw new IllegalStateException("Should not reach here.", e);
			}
		}

		Collection<FieldData> fields = data.fields();

		SortedSet<FieldLayout> result = new TreeSet<FieldLayout>();
		for (FieldData f : fields)
		{
			result.add(new FieldLayout(f, f.vmOffset(), model.sizeOf(f.typeClass())));
		}

		int instanceSize;
		if (result.isEmpty())
		{
			instanceSize = VMSupport.align(model.headerSize());
		}
		else
		{
			FieldLayout f = result.last();
			instanceSize = VMSupport.align(f.offset() + f.size());
		}
		return new ClassLayout(data, result, model.headerSize(), instanceSize, true);
	}

	@Override
	public String toString()
	{
		return "Current VM Layout";
	}

}
