package com.kayhut.fuse.dispatcher.ontolgy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by benishue on 12-Mar-17.
 */
public class OntologyUtilTest {
    @Test
    public void getEntityTypeNameById() throws Exception {
        String ontologyJSONString = "{\"ont\":\"Dragons\",\"entityTypes\":[{\"eType\":1,\"name\":\"Person\",\"properties\":[{\"pType\":1,\"name\":\"first name\",\"type\":\"string\",\"report\":[\"raw\"]},{\"pType\":2,\"name\":\"last name\",\"type\":\"string\",\"report\":[\"raw\"]},{\"pType\":3,\"name\":\"gender\",\"type\":\"TYPE_Gender\"},{\"pType\":4,\"name\":\"birth date\",\"type\":\"date\",\"report\":[\"raw\"]},{\"pType\":5,\"name\":\"death date\",\"type\":\"date\"},{\"pType\":6,\"name\":\"height\",\"type\":\"int\",\"units\":\"cm\",\"report\":[\"raw\"]}],\"display\":[\"%1 %2\",\"%4\",\"%6\"]},{\"eType\":2,\"name\":\"Dragon\",\"properties\":[{\"pType\":1,\"name\":\"name\",\"type\":\"string\",\"report\":[\"raw\"]}],\"display\":[\"name: %1\"]}],\"relationshipTypes\":[{\"rType\":1,\"name\":\"own\",\"directional\":true,\"ePairs\":[{\"eTypeA\":1,\"eTypeB\":2}],\"properties\":[{\"pType\":1,\"name\":\"since\",\"type\":\"date\",\"report\":[\"min\",\"max\"]},{\"pType\":2,\"name\":\"till\",\"type\":\"date\",\"report\":[\"min\",\"max\"]}]},{\"rType\":2,\"name\":\"fires at\",\"directional\":true,\"ePairs\":[{\"eTypeA\":2,\"eTypeB\":2}],\"properties\":[{\"pType\":1,\"name\":\"time\",\"type\":\"datetime\",\"report\":[\"min\",\"max\"]}]}],\"enumeratedTypes\":[{\"eType\":\"TYPE_Gender\",\"values\":[{\"val\":1,\"name\":\"Female\"},{\"val\":2,\"name\":\"Male\"},{\"val\":3,\"name\":\"Other\"}]}]}";
        Ontology ontology = new ObjectMapper().readValue(ontologyJSONString, Ontology.class);
        assertEquals(OntologyUtil.getEntityTypeNameById(ontology,1),"Person");
    }

}