package org.craftercms.studio.testing.rest.site;


import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.craftercms.studio.testing.base.rest.BaseRestTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.jayway.restassured.http.ContentType;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 */
public class SitesRestTest extends BaseRestTest {


    private String siteName;

    @BeforeTest
    public void buildSiteName(){
        siteName= RandomStringUtils.randomAlphabetic(7).toLowerCase();
    }

    @Test
    public void testCreateSites() throws Exception {
        given().baseUri(getBaseUrl())
            .accept(ContentType.JSON).body("{\"siteId\": \""+siteName+"\", \"siteName\": \""+siteName+"\", "
            + "\"blueprintName\": "
            + "\"corporate\", "
            + "\"description\": \"\"}")
        .post("/api/1/services/api/1/site/create-site.json")
            .then()
            .assertThat().contentType(ContentType.JSON).and().statusCode(200);
    }

    @Test(dependsOnMethods = "testCreateSites")
       public void testSites() throws Exception {
       List<HashMap<String,Object>> sites=given().baseUri(getBaseUrl()).accept
            (ContentType.JSON).get
            ("/api/1/services/api/1/user/get-sites-3.json").then().assertThat().contentType(ContentType.JSON).and()
            .statusCode(200).and().content("$", hasSize(greaterThanOrEqualTo(1))).extract().path("$");
        boolean siteFound = false;
        for (HashMap<String, Object> site : sites) {
            assertTrue(site.containsKey("siteId"),"For site"+site);
            assertTrue(site.containsKey("name"),"For site"+site);
            assertTrue(site.containsKey("siteId"),"For site"+site);
            assertTrue(site.containsKey("description"),"For site"+site);
            assertTrue(site.containsKey("id"), "For site" + site);
            assertNull(site.get("description"), "For site" + site);
            assertThat("For site" + site, (Integer)site.get("id"), is(greaterThan(0)));
            if(site.get("siteId").toString().equals(siteName)){
                assertEquals(site.get("siteId"),site.get("name"),"for site "+site);
                siteFound=true;
                break;
            }
            assertTrue(siteFound,"Site Not found"+siteName);
        }
        


    }

    @Test(dependsOnMethods = "testSites")
    public void testDeleteSites() throws Exception {
        given().baseUri(getBaseUrl())
            .accept(ContentType.JSON).body("{\"siteId\": \""+siteName+"\"}")
            .post("/api/1/services/api/1/site/delete-site.json")
            .then()
            .assertThat().contentType(ContentType.JSON).and().statusCode(200).and().body(is("true"));
        List<HashMap<String,Object>> sites=given().baseUri(getBaseUrl()).accept
            (ContentType.JSON).get
            ("/api/1/services/api/1/user/get-sites-3.json").then().assertThat().contentType(ContentType.JSON).and()
            .statusCode(200).and().extract().path("$");
        boolean siteFound = false;
        for (HashMap<String, Object> site : sites) {
            if(site.get("siteId").toString().equals(siteName)){
                siteFound=true;
                break;
            }
        }
        assertFalse(siteFound,"Site Not deleted");
    }

}
