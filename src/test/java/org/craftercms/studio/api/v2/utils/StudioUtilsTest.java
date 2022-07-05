package org.craftercms.studio.api.v2.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StudioUtilsTest {

    @Test
    public void getTopLevelFolderTest() {
        assertEquals("/site/website", StudioUtils.getTopLevelFolder("/site/website/articles/2022/test/index.xml"));
        assertEquals("/site/website", StudioUtils.getTopLevelFolder("/site/website/index.xml"));

        assertEquals("/site/components", StudioUtils.getTopLevelFolder("/site/components/articles/article1.xml"));
        assertEquals("/site/components", StudioUtils.getTopLevelFolder("/site/components/articles/2022/test/index.xml"));

        assertEquals("/site/taxonomy", StudioUtils.getTopLevelFolder("/site/taxonomy/categories"));
        assertEquals("/site/taxonomy", StudioUtils.getTopLevelFolder("/site/taxonomy/categories/tax1.xml"));

        assertEquals("/static-assets", StudioUtils.getTopLevelFolder("/static-assets/images/logos/fb.png"));
        assertEquals("/static-assets", StudioUtils.getTopLevelFolder("/static-assets/js/head/"));

        assertEquals("/templates", StudioUtils.getTopLevelFolder("/templates/web/sidebar/widgets.ftl"));
        assertEquals("/templates", StudioUtils.getTopLevelFolder("/templates/web/blog/"));

        assertEquals("/scripts", StudioUtils.getTopLevelFolder("/scripts/rest/search"));
        assertEquals("/scripts", StudioUtils.getTopLevelFolder("/scripts/filters/site.groovy"));

        assertEquals("/sources", StudioUtils.getTopLevelFolder("/sources/utils/Util.groovy"));
        assertEquals("/sources", StudioUtils.getTopLevelFolder("/sources/utils/"));

        assertNull(StudioUtils.getTopLevelFolder("/custom/path"));
        assertNull(StudioUtils.getTopLevelFolder("/"));
        assertNull(StudioUtils.getTopLevelFolder("/my-documents/private/report.doc"));
    }
}
