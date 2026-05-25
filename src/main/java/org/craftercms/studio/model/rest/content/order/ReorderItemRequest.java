/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.model.rest.content.order;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import org.craftercms.commons.validation.annotations.param.ValidExistingContentPath;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		property = "type"
)
@JsonSubTypes({
		@JsonSubTypes.Type(value = ReorderItemRequest.AddBefore.class, name = "addBefore"),
		@JsonSubTypes.Type(value = ReorderItemRequest.Insert.class, name = "insertBetween"),
		@JsonSubTypes.Type(value = ReorderItemRequest.AddAfter.class, name = "addAfter")
})
public sealed abstract class ReorderItemRequest {

	public static sealed abstract class ReferenceReorderItemRequest extends ReorderItemRequest {
		@NotBlank
		@ValidExistingContentPath
		protected String referencePath;

		public String getReferencePath() {
			return referencePath;
		}

		public void setReferencePath(String referencePath) {
			this.referencePath = referencePath;
		}
	}

	public static final class AddBefore extends ReferenceReorderItemRequest {
	}

	public static final class AddAfter extends ReferenceReorderItemRequest {
	}

	public static final class Insert extends ReorderItemRequest {
		@NotBlank
		@ValidExistingContentPath
		private String previousPath;
		@NotBlank
		@ValidExistingContentPath
		private String nextPath;

		public String getNextPath() {
			return nextPath;
		}

		public void setNextPath(String nextPath) {
			this.nextPath = nextPath;
		}

		public String getPreviousPath() {
			return previousPath;
		}

		public void setPreviousPath(String previousPath) {
			this.previousPath = previousPath;
		}
	}
}
