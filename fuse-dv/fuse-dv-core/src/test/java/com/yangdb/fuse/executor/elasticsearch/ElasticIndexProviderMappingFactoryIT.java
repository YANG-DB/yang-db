
package com.yangdb.fuse.executor.elasticsearch;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.yangdb.fuse.executor.BaseModuleInjectionTest;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.test.BaseITMarker;
import javaslang.Tuple2;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.opensearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.opensearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.opensearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.opensearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.yangdb.fuse.executor.TestSuiteIndexProviderSuite.*;

public class ElasticIndexProviderMappingFactoryIT extends BaseModuleInjectionTest implements BaseITMarker {

    @Test
    public void testGenerateNestedMapping() {
        ElasticIndexProviderMappingFactory mappingFactory = new ElasticIndexProviderMappingFactory(client, nestedSchema, ontology, nestedProvider);
        List<Tuple2<String, Boolean>> list = mappingFactory.generateMappings();
        Assert.assertEquals(list.size(),14);
        HashSet<String> indices = Sets.newHashSet("projection","fire","freeze","own","subjectof","dragon","kingdom","people","horse","guild","know","registeredin","originatedin","memberof");
        list.stream().map(i->i._1).forEach(l->Assert.assertTrue("list must contain "+l,indices.contains(l)));

        indices.forEach(index ->{
            switch (index){
                case "projection":
                    GetIndexTemplatesResponse response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("people")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"people");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\"}");
                    Assert.assertEquals("{\"Person\":{\"properties\":{\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"lastName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"Profession\":{\"type\":\"nested\",\"properties\":{\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"experience\":{\"type\":\"keyword\"},\"salary\":{\"type\":\"integer\"},\"certification\":{\"type\":\"keyword\"}}},\"gender\":{\"type\":\"keyword\"},\"deathDate\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"birthDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"height\":{\"type\":\"integer\"}}}}",response.getIndexTemplates().get(0).mappings().get("Person").toString());
                    break;
                case "person":
                case "people":
                case "Person":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("people")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"people");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\"}");
                    Assert.assertEquals("{\"Person\":{\"properties\":{\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"lastName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"Profession\":{\"type\":\"nested\",\"properties\":{\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"experience\":{\"type\":\"keyword\"},\"salary\":{\"type\":\"integer\"},\"certification\":{\"type\":\"keyword\"}}},\"gender\":{\"type\":\"keyword\"},\"deathDate\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"birthDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"height\":{\"type\":\"integer\"}}}}",response.getIndexTemplates().get(0).mappings().get("Person").toString());
                    break;
                case "horse":
                case "horses":
                case "Horse":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("horse")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"horse");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Horse").toString(),"{\"Horse\":{\"properties\":{\"distance\":{\"type\":\"integer\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"weight\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"maxSpeed\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"}}}}");
                    break;
                case "dragon":
                case "dragons":
                case "Dragon":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("dragon")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"dragon");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Dragon").toString(),"{\"Dragon\":{\"properties\":{\"gender\":{\"type\":\"keyword\"},\"color\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"power\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"},\"birthDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}");
                    break;
                case "kingdoms":
                case "kingdom":
                case "Kingdom":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("kingdom")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"kingdom");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Kingdom").toString(),"{\"Kingdom\":{\"properties\":{\"independenceDay\":{\"type\":\"keyword\"},\"king\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"queen\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"funds\":{\"type\":\"float\"},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}}}");
                    break;
                case "guilds":
                case "guild":
                case "Guild":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("guild")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"guild");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Guild").toString(),"{\"Guild\":{\"properties\":{\"iconId\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"description\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"establishDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"url\":{\"type\":\"keyword\"}}}}");
                    break;
                case "Own":
                case "own":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("own")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"own");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Own").toString(),"{\"Own\":{\"properties\":{\"entityA\":{\"properties\":{\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"endDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"direction\":{\"type\":\"keyword\"}}}}");
                    break;
                case "Know":
                case "know":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("know")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"know");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Know").toString(),"{\"Know\":{\"properties\":{\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"direction\":{\"type\":\"keyword\"}}}}");
                    break;
                case "memberof":
                case "MemberOf":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("memberof")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),index.toLowerCase());
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("MemberOf").toString(),"{\"MemberOf\":{\"properties\":{\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"endDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"direction\":{\"type\":\"keyword\"}}}}");
                    break;
                case "originatedin":
                case "OriginatedIn":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("originatedin")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),index.toLowerCase());
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("OriginatedIn").toString(),"{\"OriginatedIn\":{\"properties\":{\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"direction\":{\"type\":\"keyword\"}}}}");
                    break;
                case "subjectof":
                case "SubjectOf":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("subjectof")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),index.toLowerCase());
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("SubjectOf").toString(),"{\"SubjectOf\":{\"properties\":{\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"direction\":{\"type\":\"keyword\"}}}}");
                    break;
                case "registeredin":
                case "RegisteredIn":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("registeredin")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),index.toLowerCase());
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("RegisteredIn").toString(),"{\"RegisteredIn\":{\"properties\":{\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"direction\":{\"type\":\"keyword\"}}}}");
                    break;
                case "Fire":
                case "fire":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("fire")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),index.toLowerCase());
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Fire").toString(),"{\"Fire\":{\"properties\":{\"date\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"entityA\":{\"properties\":{\"color\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"color\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"temperature\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"direction\":{\"type\":\"keyword\"}}}}");
                    break;
                case "Freeze":
                case "freeze":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("freeze")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),index.toLowerCase());
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Freeze").toString(),"{\"Freeze\":{\"properties\":{\"date\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"entityA\":{\"properties\":{\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"temperature\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"direction\":{\"type\":\"keyword\"}}}}");
                    break;
                default:
                    Assert.fail("Not expecting non registered type "+ index);
            }
        });
    }

    @Test
    public void testGenerateEmbeddedMapping() {
        ElasticIndexProviderMappingFactory mappingFactory = new ElasticIndexProviderMappingFactory(client, embeddedSchema, ontology, embeddedProvider);
        List<Tuple2<String, Boolean>> list = mappingFactory.generateMappings();
        Assert.assertEquals(list.size(),14);
        HashSet<String> indices = Sets.newHashSet("projection","fire","freeze","own","subjectof","dragon","kingdom","people","horse","guild","know","registeredin","originatedin","memberof");
        list.stream().map(i->i._1).forEach(l->Assert.assertTrue("list must contain "+l,indices.contains(l)));

        indices.forEach(index ->{
            switch (index){
                case "Projection":
                case "projection":
                    GetIndexTemplatesResponse response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("projection")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"projection");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("projection").toString(),"{\"projection\":{\"properties\":{\"Profession\":{\"type\":\"nested\",\"properties\":{\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"experience\":{\"type\":\"keyword\"},\"salary\":{\"type\":\"integer\"},\"certification\":{\"type\":\"keyword\"}}},\"Kingdom\":{\"type\":\"nested\",\"properties\":{\"independenceDay\":{\"type\":\"keyword\"},\"king\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"queen\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"funds\":{\"type\":\"float\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"Horse\":{\"type\":\"nested\",\"properties\":{\"distance\":{\"type\":\"integer\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"weight\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"maxSpeed\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"},\"OriginatedIn\":{\"type\":\"nested\",\"properties\":{\"entityB.type\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}},\"RegisteredIn\":{\"type\":\"nested\",\"properties\":{\"entityB.type\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}},\"Guild\":{\"type\":\"nested\",\"properties\":{\"iconId\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"description\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"establishDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"url\":{\"type\":\"keyword\"},\"RegisteredIn\":{\"type\":\"nested\",\"properties\":{\"entityB.type\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}},\"Person\":{\"type\":\"nested\",\"properties\":{\"lastName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"HasProfession\":{\"type\":\"nested\",\"properties\":{\"entityB.type\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"}}},\"gender\":{\"type\":\"keyword\"},\"Own\":{\"type\":\"nested\",\"properties\":{\"entityB.type\":{\"type\":\"keyword\"},\"endDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}},\"Know\":{\"type\":\"nested\",\"properties\":{\"entityB.type\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}},\"type\":{\"type\":\"keyword\"},\"birthDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"OriginatedIn\":{\"type\":\"nested\",\"properties\":{\"entityB.type\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}},\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"MemberOf\":{\"type\":\"nested\",\"properties\":{\"entityB.type\":{\"type\":\"keyword\"},\"endDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}},\"deathDate\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"SubjectOf\":{\"type\":\"nested\",\"properties\":{\"entityB.type\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"height\":{\"type\":\"integer\"}}},\"Dragon\":{\"type\":\"nested\",\"properties\":{\"gender\":{\"type\":\"keyword\"},\"color\":{\"type\":\"keyword\"},\"Fire\":{\"type\":\"nested\",\"properties\":{\"date\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"entityB.type\":{\"type\":\"keyword\"},\"temperature\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"power\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"},\"birthDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"Freeze\":{\"type\":\"nested\",\"properties\":{\"date\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"entityB.type\":{\"type\":\"keyword\"},\"temperature\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"OriginatedIn\":{\"type\":\"nested\",\"properties\":{\"entityB.type\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}},\"queryId\":{\"type\":\"keyword\"},\"cursorId\":{\"type\":\"keyword\"},\"timestamp\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}");
                    break;
                case "person":
                case "people":
                case "Person":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("people")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"people");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\"}");
                    Assert.assertEquals("{\"Person\":{\"properties\":{\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"lastName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"Profession\":{\"properties\":{\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"experience\":{\"type\":\"keyword\"},\"salary\":{\"type\":\"integer\"},\"certification\":{\"type\":\"keyword\"}}},\"gender\":{\"type\":\"keyword\"},\"deathDate\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"birthDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"height\":{\"type\":\"integer\"}}}}",response.getIndexTemplates().get(0).mappings().get("Person").toString());
                    break;
                case "horse":
                case "horses":
                case "Horse":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("horse")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"horse");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Horse").toString(),"{\"Horse\":{\"properties\":{\"distance\":{\"type\":\"integer\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"weight\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"maxSpeed\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"}}}}");
                    break;
                case "dragon":
                case "dragons":
                case "Dragon":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("dragon")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"dragon");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Dragon").toString(),"{\"Dragon\":{\"properties\":{\"gender\":{\"type\":\"keyword\"},\"color\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"power\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"},\"birthDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}");
                    break;
                case "kingdoms":
                case "kingdom":
                case "Kingdom":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("kingdom")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"kingdom");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Kingdom").toString(),"{\"Kingdom\":{\"properties\":{\"independenceDay\":{\"type\":\"keyword\"},\"king\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"queen\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"funds\":{\"type\":\"float\"},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}}}");
                    break;
                case "guilds":
                case "guild":
                case "Guild":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("guild")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"guild");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Guild").toString(),"{\"Guild\":{\"properties\":{\"iconId\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"description\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"establishDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"url\":{\"type\":\"keyword\"}}}}");
                    break;
                case "Own":
                case "own":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("own")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"own");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Own").toString(),"{\"Own\":{\"properties\":{\"entityA\":{\"properties\":{\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"endDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"direction\":{\"type\":\"keyword\"}}}}");
                    break;
                case "Know":
                case "know":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("know")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"know");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Know").toString(),"{\"Know\":{\"properties\":{\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"direction\":{\"type\":\"keyword\"}}}}");
                    break;
                case "memberof":
                case "MemberOf":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("memberof")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),index.toLowerCase());
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("MemberOf").toString(),"{\"MemberOf\":{\"properties\":{\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"endDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"direction\":{\"type\":\"keyword\"}}}}");
                    break;
                case "originatedin":
                case "OriginatedIn":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("originatedin")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),index.toLowerCase());
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("OriginatedIn").toString(),"{\"OriginatedIn\":{\"properties\":{\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"direction\":{\"type\":\"keyword\"}}}}");
                    break;
                case "subjectof":
                case "SubjectOf":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("subjectof")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),index.toLowerCase());
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("SubjectOf").toString(),"{\"SubjectOf\":{\"properties\":{\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"direction\":{\"type\":\"keyword\"}}}}");
                    break;
                case "registeredin":
                case "RegisteredIn":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("registeredin")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),index.toLowerCase());
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("RegisteredIn").toString(),"{\"RegisteredIn\":{\"properties\":{\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"direction\":{\"type\":\"keyword\"}}}}");
                    break;
                case "Fire":
                case "fire":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("fire")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),index.toLowerCase());
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Fire").toString(),"{\"Fire\":{\"properties\":{\"date\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"entityA\":{\"properties\":{\"color\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"color\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"temperature\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"direction\":{\"type\":\"keyword\"}}}}");
                    break;
                case "Freeze":
                case "freeze":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("freeze")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),index.toLowerCase());
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Freeze").toString(),"{\"Freeze\":{\"properties\":{\"date\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"entityA\":{\"properties\":{\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"temperature\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"direction\":{\"type\":\"keyword\"}}}}");
                    break;
                default:
                    Assert.fail("Not expecting non registered type "+ index);
            }
        });
    }

    @Test
    public void testGenerateSingleMapping() {
        ElasticIndexProviderMappingFactory mappingFactory = new ElasticIndexProviderMappingFactory(client, singleIndexSchema, ontology, singleIndexProvider);
        List<Tuple2<String, Boolean>> list = mappingFactory.generateMappings();
        Assert.assertEquals(list.size(),2);
        List<String> indices = Arrays.asList("ontology","projection");

        Assert.assertEquals(list.stream().map(i->i._1).collect(Collectors.toSet()), new HashSet<>(indices));

        indices.forEach(index ->{
            switch (index){
                case "Projection":
                case "projection":
                    GetIndexTemplatesResponse response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("projection")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"projection");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("projection").toString(),"{\"projection\":{\"properties\":{\"Profession\":{\"type\":\"nested\",\"properties\":{\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"experience\":{\"type\":\"keyword\"},\"salary\":{\"type\":\"integer\"},\"certification\":{\"type\":\"keyword\"}}},\"Kingdom\":{\"type\":\"nested\",\"properties\":{\"independenceDay\":{\"type\":\"keyword\"},\"king\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"queen\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"funds\":{\"type\":\"float\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"Horse\":{\"type\":\"nested\",\"properties\":{\"distance\":{\"type\":\"integer\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"weight\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"maxSpeed\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"},\"OriginatedIn\":{\"type\":\"nested\",\"properties\":{\"entityB.type\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}},\"RegisteredIn\":{\"type\":\"nested\",\"properties\":{\"entityB.type\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}},\"Guild\":{\"type\":\"nested\",\"properties\":{\"iconId\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"description\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"establishDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"url\":{\"type\":\"keyword\"},\"RegisteredIn\":{\"type\":\"nested\",\"properties\":{\"entityB.type\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}},\"Person\":{\"type\":\"nested\",\"properties\":{\"lastName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"HasProfession\":{\"type\":\"nested\",\"properties\":{\"entityB.type\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"}}},\"gender\":{\"type\":\"keyword\"},\"Own\":{\"type\":\"nested\",\"properties\":{\"entityB.type\":{\"type\":\"keyword\"},\"endDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}},\"Know\":{\"type\":\"nested\",\"properties\":{\"entityB.type\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}},\"type\":{\"type\":\"keyword\"},\"birthDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"OriginatedIn\":{\"type\":\"nested\",\"properties\":{\"entityB.type\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}},\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"MemberOf\":{\"type\":\"nested\",\"properties\":{\"entityB.type\":{\"type\":\"keyword\"},\"endDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}},\"deathDate\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"SubjectOf\":{\"type\":\"nested\",\"properties\":{\"entityB.type\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"height\":{\"type\":\"integer\"}}},\"Dragon\":{\"type\":\"nested\",\"properties\":{\"gender\":{\"type\":\"keyword\"},\"color\":{\"type\":\"keyword\"},\"Fire\":{\"type\":\"nested\",\"properties\":{\"date\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"entityB.type\":{\"type\":\"keyword\"},\"temperature\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"power\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"},\"birthDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"Freeze\":{\"type\":\"nested\",\"properties\":{\"date\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"entityB.type\":{\"type\":\"keyword\"},\"temperature\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"OriginatedIn\":{\"type\":\"nested\",\"properties\":{\"entityB.type\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"tag\":{\"type\":\"keyword\"},\"entityB.id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}},\"queryId\":{\"type\":\"keyword\"},\"cursorId\":{\"type\":\"keyword\"},\"timestamp\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}");
                    break;
                case "ontology":
                case "Ontology":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("ontology")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"ontology");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("ontology").toString(),"{\"ontology\":{\"properties\":{\"date\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"lastName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"independenceDay\":{\"type\":\"keyword\"},\"gender\":{\"type\":\"keyword\"},\"distance\":{\"type\":\"integer\"},\"color\":{\"type\":\"keyword\"},\"queen\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"endDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"description\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"},\"deathDate\":{\"type\":\"keyword\"},\"temperature\":{\"type\":\"integer\"},\"funds\":{\"type\":\"float\"},\"id\":{\"type\":\"keyword\"},\"power\":{\"type\":\"integer\"},\"height\":{\"type\":\"integer\"},\"iconId\":{\"type\":\"keyword\"},\"king\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"weight\":{\"type\":\"integer\"},\"maxSpeed\":{\"type\":\"integer\"},\"birthDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"establishDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"url\":{\"type\":\"keyword\"},\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"Profession\":{\"type\":\"nested\",\"properties\":{\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"experience\":{\"type\":\"keyword\"},\"salary\":{\"type\":\"integer\"},\"certification\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}");
                    break;
                default:
                    Assert.fail("Not expecting non registered type "+ index);
            }
        });
    }

    @Test
    public void createNestedIndicesTest() {
        RawSchema schema = nestedSchema;
        ElasticIndexProviderMappingFactory mappingFactory = new ElasticIndexProviderMappingFactory(client, schema, ontology, nestedProvider);
        List<Tuple2<String, Boolean>> list = mappingFactory.generateMappings();
        Assert.assertEquals(list.size(),14);
        HashSet<String> labels = Sets.newHashSet("projection","fire","freeze","own","subjectof","dragon","kingdom","people","horse","guild","know","registeredin","originatedin","memberof");
        list.stream().map(i->i._1).forEach(l->Assert.assertTrue("list must contain "+l,labels.contains(l)));

        Iterable<String> allIndices = schema.indices();
        javaslang.collection.Stream.ofAll(allIndices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());

        Set<Tuple2<Boolean,String>> indices =  new HashSet<>(mappingFactory.createIndices());
        Set<String> names = new HashSet<>(Arrays.asList("idx_fire_500","idx_freeze_2000","idx_fire_1500","idx_freeze_1000","own","subjectof","dragon","idx_freeze_1500","idx_fire_2000","kingdom","people","idx_fire_1000","horse","guild","idx_freeze_500","know","registeredin","originatedin","memberof"));

        Assert.assertEquals(indices.stream().map(i->i._2).collect(Collectors.toSet()), names);
        indices.forEach(index -> {
            GetMappingsRequest request = new GetMappingsRequest();
            request.indices(index._2);
            GetMappingsResponse response = client.admin().indices().getMappings(request).actionGet();
            switch (index._2) {
                case "Projection":
                case "projection":
                    Assert.assertEquals(response.toString(),"{\"own\":{\"mappings\":{\"Own\":{\"properties\":{\"direction\":{\"type\":\"keyword\"},\"endDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"entityA\":{\"properties\":{\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"startDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "Own":
                case "own":
                    Assert.assertEquals(response.toString(),"{\"own\":{\"mappings\":{\"Own\":{\"properties\":{\"direction\":{\"type\":\"keyword\"},\"endDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"entityA\":{\"properties\":{\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"startDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "Know":
                case "know":
                    Assert.assertEquals(response.toString(),"{\"know\":{\"mappings\":{\"Know\":{\"properties\":{\"direction\":{\"type\":\"keyword\"},\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"startDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "memberOf":
                case "memberof":
                    Assert.assertEquals(response.toString(),"{\"memberof\":{\"mappings\":{\"MemberOf\":{\"properties\":{\"direction\":{\"type\":\"keyword\"},\"endDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"startDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "idx_fire_500":
                case "idx_fire_1000":
                case "idx_fire_1500":
                case "idx_fire_2000":
                case "Fire":
                case "fire":
                    try {
                        Map map = mapper.readValue(response.toString(), Map.class);
                        Assert.assertNotNull(map.get(index._2));
                        Assert.assertEquals(map.get(index._2).toString(),"{mappings={Fire={properties={date={type=date, format=epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS}, direction={type=keyword}, entityA={properties={color={type=keyword}, id={type=keyword}, name={type=text, fields={keyword={type=keyword}}}, type={type=keyword}}}, entityB={properties={color={type=keyword}, id={type=keyword}, name={type=text, fields={keyword={type=keyword}}}, type={type=keyword}}}, id={type=keyword}, temperature={type=integer}, type={type=keyword}}}}}");
                    } catch (IOException e) {
                        Assert.fail("Not expecting non registered type "+ index);
                    }
                    break;
                case "idx_freeze_500":
                case "idx_freeze_1000":
                case "idx_freeze_1500":
                case "idx_freeze_2000":
                case "Freeze":
                case "freeze":
                    try {
                        Map map = mapper.readValue(response.toString(), Map.class);
                        Assert.assertEquals(map.get(index._2).toString(),"{mappings={Freeze={properties={date={type=date, format=epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS}, direction={type=keyword}, entityA={properties={id={type=keyword}, name={type=text, fields={keyword={type=keyword}}}, type={type=keyword}}}, entityB={properties={id={type=keyword}, name={type=text, fields={keyword={type=keyword}}}, type={type=keyword}}}, id={type=keyword}, temperature={type=integer}, type={type=keyword}}}}}");
                    } catch (IOException e) {
                        Assert.fail("Not expecting non registered type "+ index);
                    }
                    break;
                case "originatedIn":
                case "originatedin":
                    Assert.assertEquals(response.toString(),"{\"originatedin\":{\"mappings\":{\"OriginatedIn\":{\"properties\":{\"direction\":{\"type\":\"keyword\"},\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"startDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "subjectOf":
                case "subjectof":
                    Assert.assertEquals(response.toString(),"{\"subjectof\":{\"mappings\":{\"SubjectOf\":{\"properties\":{\"direction\":{\"type\":\"keyword\"},\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"startDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "registeredIn":
                case "registeredin":
                    Assert.assertEquals(response.toString(),"{\"registeredin\":{\"mappings\":{\"RegisteredIn\":{\"properties\":{\"direction\":{\"type\":\"keyword\"},\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"startDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "person":
                case "people":
                    Assert.assertEquals("{\"people\":{\"mappings\":{\"Person\":{\"properties\":{\"Profession\":{\"type\":\"nested\",\"properties\":{\"certification\":{\"type\":\"keyword\"},\"experience\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"salary\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"}}},\"birthDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"deathDate\":{\"type\":\"keyword\"},\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"gender\":{\"type\":\"keyword\"},\"height\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"lastName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}}}}}",response.toString());
                    break;
                case "Horse":
                case "horse":
                case "horses":
                    Assert.assertEquals(response.toString(),"{\"horse\":{\"mappings\":{\"Horse\":{\"properties\":{\"distance\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"maxSpeed\":{\"type\":\"integer\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"},\"weight\":{\"type\":\"integer\"}}}}}}");
                    break;
                case "Dragon":
                case "dragon":
                case "dragons":
                    Assert.assertEquals(response.toString(),"{\"dragon\":{\"mappings\":{\"Dragon\":{\"properties\":{\"birthDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"color\":{\"type\":\"keyword\"},\"gender\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"power\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "kingdom":
                case "kingdoms":
                    Assert.assertEquals(response.toString(),"{\"kingdom\":{\"mappings\":{\"Kingdom\":{\"properties\":{\"funds\":{\"type\":\"float\"},\"id\":{\"type\":\"keyword\"},\"independenceDay\":{\"type\":\"keyword\"},\"king\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"queen\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "guild":
                case "guilds":
                    Assert.assertEquals(response.toString(),"{\"guild\":{\"mappings\":{\"Guild\":{\"properties\":{\"description\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"establishDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"iconId\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"},\"url\":{\"type\":\"keyword\"}}}}}}");
                    break;
                default:
                    Assert.fail("Not expecting non registered type "+ index);
            }
        });


    }

    @Test
    public void createEmbeddedIndicesTest() {
        RawSchema schema = embeddedSchema;
        ElasticIndexProviderMappingFactory mappingFactory = new ElasticIndexProviderMappingFactory(client, schema, ontology, embeddedProvider);
        List<Tuple2<String, Boolean>> list = mappingFactory.generateMappings();
        Assert.assertEquals(list.size(),14);
        HashSet<String> labels = Sets.newHashSet("projection","fire","freeze","own","subjectof","dragon","kingdom","people","horse","guild","know","registeredin","originatedin","memberof");
        list.stream().map(i->i._1).forEach(l->Assert.assertTrue("list must contain "+l,labels.contains(l)));

        Iterable<String> allIndices = schema.indices();
        javaslang.collection.Stream.ofAll(allIndices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());

        Set<Tuple2<Boolean,String>> indices =  new HashSet<>(mappingFactory.createIndices());
        Set<String> names = new HashSet<>(Arrays.asList("idx_fire_500","idx_freeze_2000","idx_fire_1500","idx_freeze_1000","own","subjectof","dragon","idx_freeze_1500","idx_fire_2000","kingdom","people","idx_fire_1000","horse","guild","idx_freeze_500","know","registeredin","originatedin","memberof"));

        Assert.assertEquals(indices.stream().map(i->i._2).collect(Collectors.toSet()), names);
        indices.forEach(index -> {
            GetMappingsRequest request = new GetMappingsRequest();
            request.indices(index._2);
            GetMappingsResponse response = client.admin().indices().getMappings(request).actionGet();
            switch (index._2) {
                case "Own":
                case "own":
                    Assert.assertEquals(response.toString(),"{\"own\":{\"mappings\":{\"Own\":{\"properties\":{\"direction\":{\"type\":\"keyword\"},\"endDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"entityA\":{\"properties\":{\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"startDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "Know":
                case "know":
                    Assert.assertEquals(response.toString(),"{\"know\":{\"mappings\":{\"Know\":{\"properties\":{\"direction\":{\"type\":\"keyword\"},\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"startDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "memberOf":
                case "memberof":
                    Assert.assertEquals(response.toString(),"{\"memberof\":{\"mappings\":{\"MemberOf\":{\"properties\":{\"direction\":{\"type\":\"keyword\"},\"endDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"startDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "idx_fire_500":
                case "idx_fire_1000":
                case "idx_fire_1500":
                case "idx_fire_2000":
                case "Fire":
                case "fire":
                    try {
                        Map map = mapper.readValue(response.toString(), Map.class);
                        Assert.assertEquals(map.get(index._2).toString(),"{mappings={Fire={properties={date={type=date, format=epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS}, direction={type=keyword}, entityA={properties={color={type=keyword}, id={type=keyword}, name={type=text, fields={keyword={type=keyword}}}, type={type=keyword}}}, entityB={properties={color={type=keyword}, id={type=keyword}, name={type=text, fields={keyword={type=keyword}}}, type={type=keyword}}}, id={type=keyword}, temperature={type=integer}, type={type=keyword}}}}}");
                    } catch (IOException e) {
                        Assert.fail("Not expecting non registered type "+ index);
                    }
                    break;
                case "idx_freeze_500":
                case "idx_freeze_1000":
                case "idx_freeze_1500":
                case "idx_freeze_2000":
                case "Freeze":
                case "freeze":
                    try {
                        Map map = mapper.readValue(response.toString(), Map.class);
                        Assert.assertEquals(map.get(index._2).toString(),"{mappings={Freeze={properties={date={type=date, format=epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS}, direction={type=keyword}, entityA={properties={id={type=keyword}, name={type=text, fields={keyword={type=keyword}}}, type={type=keyword}}}, entityB={properties={id={type=keyword}, name={type=text, fields={keyword={type=keyword}}}, type={type=keyword}}}, id={type=keyword}, temperature={type=integer}, type={type=keyword}}}}}");
                    } catch (IOException e) {
                        Assert.fail("Not expecting non registered type "+ index);
                    }
                    break;
                case "originatedIn":
                case "originatedin":
                    Assert.assertEquals(response.toString(),"{\"originatedin\":{\"mappings\":{\"OriginatedIn\":{\"properties\":{\"direction\":{\"type\":\"keyword\"},\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"startDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "subjectOf":
                case "subjectof":
                    Assert.assertEquals(response.toString(),"{\"subjectof\":{\"mappings\":{\"SubjectOf\":{\"properties\":{\"direction\":{\"type\":\"keyword\"},\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"startDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "registeredIn":
                case "registeredin":
                    Assert.assertEquals(response.toString(),"{\"registeredin\":{\"mappings\":{\"RegisteredIn\":{\"properties\":{\"direction\":{\"type\":\"keyword\"},\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"startDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "person":
                case "people":
                    Assert.assertEquals("{\"people\":{\"mappings\":{\"Person\":{\"properties\":{\"Profession\":{\"properties\":{\"certification\":{\"type\":\"keyword\"},\"experience\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"salary\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"}}},\"birthDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"deathDate\":{\"type\":\"keyword\"},\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"gender\":{\"type\":\"keyword\"},\"height\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"lastName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}}}}}",response.toString());
                    break;
                case "horse":
                case "horses":
                    Assert.assertEquals(response.toString(),"{\"horse\":{\"mappings\":{\"Horse\":{\"properties\":{\"distance\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"maxSpeed\":{\"type\":\"integer\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"},\"weight\":{\"type\":\"integer\"}}}}}}");
                    break;
                case "dragon":
                case "dragons":
                    Assert.assertEquals(response.toString(),"{\"dragon\":{\"mappings\":{\"Dragon\":{\"properties\":{\"birthDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"color\":{\"type\":\"keyword\"},\"gender\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"power\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "kingdom":
                case "kingdoms":
                    Assert.assertEquals(response.toString(),"{\"kingdom\":{\"mappings\":{\"Kingdom\":{\"properties\":{\"funds\":{\"type\":\"float\"},\"id\":{\"type\":\"keyword\"},\"independenceDay\":{\"type\":\"keyword\"},\"king\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"queen\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "guild":
                case "guilds":
                    Assert.assertEquals(response.toString(),"{\"guild\":{\"mappings\":{\"Guild\":{\"properties\":{\"description\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"establishDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"iconId\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"},\"url\":{\"type\":\"keyword\"}}}}}}");
                    break;
                default:
                    Assert.fail("Not expecting non registered type "+ index);
            }
        });


    }

    @Test
    @Ignore("Todo - fix template pattern naming on ElasticIndexProviderMappingFactory(lines:152,178) ")
    public void createSingleIndicesTest() {
        RawSchema schema = singleIndexSchema;
        ElasticIndexProviderMappingFactory mappingFactory = new ElasticIndexProviderMappingFactory(client, schema, ontology, singleIndexProvider);
        List<Tuple2<String, Boolean>> list = mappingFactory.generateMappings();
        Assert.assertEquals(list.size(),1);
        HashSet<String> labels = Sets.newHashSet("ontology");

        Assert.assertEquals(list.stream().map(i->i._1).collect(Collectors.toSet()), labels);

        Iterable<String> allIndices = schema.indices();
        javaslang.collection.Stream.ofAll(allIndices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());

        Set<Tuple2<Boolean,String>> indices =  new HashSet<>(mappingFactory.createIndices());
        Set<String> names = new HashSet<>(ImmutableList.of("ontology"));

        Assert.assertEquals(indices.stream().map(i->i._2).collect(Collectors.toSet()), names);
        indices.forEach(index -> {
            GetMappingsRequest request = new GetMappingsRequest();
            request.indices(index._2);
            GetMappingsResponse response = client.admin().indices().getMappings(request).actionGet();
            switch (index._2) {
                case "Ontology":
                case "ontology":
                    Assert.assertEquals(response.toString(),"{\"ontology\":{\"mappings\":{\"ontology\":{\"properties\":{\"birthDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"color\":{\"type\":\"keyword\"},\"date\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"deathDate\":{\"type\":\"keyword\"},\"description\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"distance\":{\"type\":\"integer\"},\"endDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"establishDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"funds\":{\"type\":\"float\"},\"gender\":{\"type\":\"keyword\"},\"height\":{\"type\":\"integer\"},\"iconId\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"independenceDay\":{\"type\":\"keyword\"},\"king\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"lastName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"maxSpeed\":{\"type\":\"integer\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"power\":{\"type\":\"integer\"},\"profession\":{\"type\":\"nested\",\"properties\":{\"certification\":{\"type\":\"keyword\"},\"experience\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"salary\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"}}},\"queen\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"startDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"temperature\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"},\"url\":{\"type\":\"keyword\"},\"weight\":{\"type\":\"integer\"}}}}}}");
                    break;
                default:
                    Assert.fail("Not expecting non registered type "+ index);
            }
        });


    }
}
