
package com.yangdb.fuse.executor.elasticsearch;

import com.google.common.collect.Sets;
import com.yangdb.fuse.executor.BaseModuleInjectionTest;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.test.BaseITMarker;
import javaslang.Tuple2;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.junit.Assert;
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
        Assert.assertEquals(list.size(),13);
        List<String> indices = Arrays.asList("person", "horse", "dragon","fire","freeze",
                "kingdom", "guild", "own", "know", "memberof", "originatedin", "subjectof", "registeredin");

        Assert.assertEquals(list.stream().map(i->i._1).collect(Collectors.toSet()), new HashSet<>(indices));

        indices.forEach(index ->{
            switch (index){
                case "person":
                case "people":
                case "Person":
                    GetIndexTemplatesResponse response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("people")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"people");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Person").toString(),"{\"Person\":{\"properties\":{\"profession\":{\"type\":\"nested\",\"properties\":{\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"experience\":{\"type\":\"keyword\"},\"salary\":{\"type\":\"integer\"},\"certification\":{\"type\":\"keyword\"}}},\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"lastName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"gender\":{\"type\":\"keyword\"},\"deathDate\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"birthDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"height\":{\"type\":\"integer\"}}}}");
                    break;
                case "horse":
                case "horses":
                case "Horse":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("horses")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"horses");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Horse").toString(),"{\"Horse\":{\"properties\":{\"distance\":{\"type\":\"integer\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"weight\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"maxSpeed\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"}}}}");
                    break;
                case "dragon":
                case "dragons":
                case "Dragon":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("dragons")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"dragons");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Dragon").toString(),"{\"Dragon\":{\"properties\":{\"gender\":{\"type\":\"keyword\"},\"color\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"power\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"},\"birthDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}");
                    break;
                case "kingdoms":
                case "kingdom":
                case "Kingdom":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("kingdoms")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"kingdoms");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Kingdom").toString(),"{\"Kingdom\":{\"properties\":{\"independenceDay\":{\"type\":\"keyword\"},\"king\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"queen\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"funds\":{\"type\":\"float\"},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}}}");
                    break;
                case "guilds":
                case "guild":
                case "Guild":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("guilds")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"guilds");
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
                    Assert.fail("Not expecting non registered type " + index);
            }
        });
    }

    @Test
    public void testGenerateEmbeddedMapping() {
        ElasticIndexProviderMappingFactory mappingFactory = new ElasticIndexProviderMappingFactory(client, embeddedSchema, ontology, embeddedProvider);
        List<Tuple2<String, Boolean>> list = mappingFactory.generateMappings();
        Assert.assertEquals(list.size(),13);
        List<String> indices = Arrays.asList("person", "horse", "dragon","fire","freeze",
                "kingdom", "guild", "own", "know", "memberof", "originatedin", "subjectof", "registeredin");

        Assert.assertEquals(list.stream().map(i->i._1).collect(Collectors.toSet()), new HashSet<>(indices));

        indices.forEach(index ->{
            switch (index){
                case "person":
                case "people":
                case "Person":
                    GetIndexTemplatesResponse response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("people")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"people");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Person").toString(),"{\"Person\":{\"properties\":{\"profession\":{\"properties\":{\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"experience\":{\"type\":\"keyword\"},\"salary\":{\"type\":\"integer\"},\"certification\":{\"type\":\"keyword\"}}},\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"lastName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"gender\":{\"type\":\"keyword\"},\"deathDate\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"birthDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"height\":{\"type\":\"integer\"}}}}");
                    break;
                case "horse":
                case "horses":
                case "Horse":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("horses")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"horses");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Horse").toString(),"{\"Horse\":{\"properties\":{\"distance\":{\"type\":\"integer\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"weight\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"maxSpeed\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"}}}}");
                    break;
                case "dragon":
                case "dragons":
                case "Dragon":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("dragons")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"dragons");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Dragon").toString(),"{\"Dragon\":{\"properties\":{\"gender\":{\"type\":\"keyword\"},\"color\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"power\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"},\"birthDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}");
                    break;
                case "kingdoms":
                case "kingdom":
                case "Kingdom":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("kingdoms")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"kingdoms");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Kingdom").toString(),"{\"Kingdom\":{\"properties\":{\"independenceDay\":{\"type\":\"keyword\"},\"king\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"queen\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"funds\":{\"type\":\"float\"},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}}}");
                    break;
                case "guilds":
                case "guild":
                case "Guild":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("guilds")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"guilds");
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
                    Assert.fail("Not expecting non registered type " + index);
            }
        });
    }

    @Test
    public void testGenerateSingleMapping() {
        ElasticIndexProviderMappingFactory mappingFactory = new ElasticIndexProviderMappingFactory(client, singleIndexSchema, ontology, singleIndexProvider);
        List<Tuple2<String, Boolean>> list = mappingFactory.generateMappings();
        Assert.assertEquals(list.size(),13);
        List<String> indices = Arrays.asList("person", "horse", "dragon","fire","freeze",
                "kingdom", "guild", "own", "know", "memberof", "originatedin", "subjectof", "registeredin");

        Assert.assertEquals(list.stream().map(i->i._1).collect(Collectors.toSet()), new HashSet<>(indices));

        indices.forEach(index ->{
            switch (index){
                case "person":
                case "people":
                case "Person":
                    GetIndexTemplatesResponse response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("people")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"people");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Person").toString(),"{\"Person\":{\"properties\":{\"profession\":{\"properties\":{\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"experience\":{\"type\":\"keyword\"},\"salary\":{\"type\":\"integer\"},\"certification\":{\"type\":\"keyword\"}}},\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"lastName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"gender\":{\"type\":\"keyword\"},\"deathDate\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"birthDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"height\":{\"type\":\"integer\"}}}}");
                    break;
                case "horse":
                case "horses":
                case "Horse":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("horses")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"horses");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Horse").toString(),"{\"Horse\":{\"properties\":{\"distance\":{\"type\":\"integer\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"weight\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"maxSpeed\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"}}}}");
                    break;
                case "dragon":
                case "dragons":
                case "Dragon":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("dragons")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"dragons");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Dragon").toString(),"{\"Dragon\":{\"properties\":{\"gender\":{\"type\":\"keyword\"},\"color\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"power\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"},\"birthDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}");
                    break;
                case "kingdoms":
                case "kingdom":
                case "Kingdom":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("kingdoms")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"kingdoms");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Kingdom").toString(),"{\"Kingdom\":{\"properties\":{\"independenceDay\":{\"type\":\"keyword\"},\"king\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"queen\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"funds\":{\"type\":\"float\"},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}}}");
                    break;
                case "guilds":
                case "guild":
                case "Guild":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("guilds")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"guilds");
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
                    Assert.fail("Not expecting non registered type " + index);
            }
        });
    }

    @Test
    public void createNestedIndicesTest() {
        RawSchema schema = nestedSchema;
        ElasticIndexProviderMappingFactory mappingFactory = new ElasticIndexProviderMappingFactory(client, schema, ontology, nestedProvider);
        List<Tuple2<String, Boolean>> list = mappingFactory.generateMappings();
        Assert.assertEquals(list.size(),13);
        HashSet<String> labels = Sets.newHashSet("person", "horse", "dragon", "fire", "freeze",
                "kingdom", "guild", "own", "know", "memberof", "originatedin", "subjectof", "registeredin");

        Assert.assertEquals(list.stream().map(i->i._1).collect(Collectors.toSet()), labels);

        Iterable<String> allIndices = schema.indices();
        javaslang.collection.Stream.ofAll(allIndices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());

        Set<String> indices =  new HashSet<>(mappingFactory.createIndices());
        Set<String> names = new HashSet<>(Arrays.asList("idx_fire_500", "idx_freeze_2000", "idx_fire_1500", "idx_freeze_1000", "guilds", "own", "subjectof", "idx_freeze_1500", "people", "idx_fire_2000", "idx_fire_1000", "idx_freeze_500", "kingdoms", "know", "registeredin", "originatedin", "memberof", "horses", "dragons"));

        Assert.assertEquals(indices, names);
        indices.forEach(index -> {
            GetMappingsRequest request = new GetMappingsRequest();
            request.indices(index);
            GetMappingsResponse response = client.admin().indices().getMappings(request).actionGet();
            switch (index) {
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
                        Assert.assertEquals(map.get(index).toString(),"{mappings={Fire={properties={date={type=date, format=epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS}, direction={type=keyword}, entityA={properties={color={type=keyword}, id={type=keyword}, name={type=text, fields={keyword={type=keyword}}}, type={type=keyword}}}, entityB={properties={color={type=keyword}, id={type=keyword}, name={type=text, fields={keyword={type=keyword}}}, type={type=keyword}}}, id={type=keyword}, temperature={type=integer}, type={type=keyword}}}}}");
                    } catch (IOException e) {
                        Assert.fail("Not expecting non registered type " + index);
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
                        Assert.assertEquals(map.get(index).toString(),"{mappings={Freeze={properties={date={type=date, format=epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS}, direction={type=keyword}, entityA={properties={id={type=keyword}, name={type=text, fields={keyword={type=keyword}}}, type={type=keyword}}}, entityB={properties={id={type=keyword}, name={type=text, fields={keyword={type=keyword}}}, type={type=keyword}}}, id={type=keyword}, temperature={type=integer}, type={type=keyword}}}}}");
                    } catch (IOException e) {
                        Assert.fail("Not expecting non registered type " + index);
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
                case "Person":
                case "people":
                    Assert.assertEquals(response.toString(),"{\"people\":{\"mappings\":{\"Person\":{\"properties\":{\"birthDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"deathDate\":{\"type\":\"keyword\"},\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"gender\":{\"type\":\"keyword\"},\"height\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"lastName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"profession\":{\"type\":\"nested\",\"properties\":{\"certification\":{\"type\":\"keyword\"},\"experience\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"salary\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "Horse":
                case "horses":
                    Assert.assertEquals(response.toString(),"{\"horses\":{\"mappings\":{\"Horse\":{\"properties\":{\"distance\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"maxSpeed\":{\"type\":\"integer\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"},\"weight\":{\"type\":\"integer\"}}}}}}");
                    break;
                case "Dragon":
                case "dragons":
                    Assert.assertEquals(response.toString(),"{\"dragons\":{\"mappings\":{\"Dragon\":{\"properties\":{\"birthDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"color\":{\"type\":\"keyword\"},\"gender\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"power\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "Kingdom":
                case "kingdoms":
                    Assert.assertEquals(response.toString(),"{\"kingdoms\":{\"mappings\":{\"Kingdom\":{\"properties\":{\"funds\":{\"type\":\"float\"},\"id\":{\"type\":\"keyword\"},\"independenceDay\":{\"type\":\"keyword\"},\"king\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"queen\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "Guilds":
                case "guilds":
                    Assert.assertEquals(response.toString(),"{\"guilds\":{\"mappings\":{\"Guild\":{\"properties\":{\"description\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"establishDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"iconId\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"},\"url\":{\"type\":\"keyword\"}}}}}}");
                    break;
                default:
                    Assert.fail("Not expecting non registered type " + index);
            }
        });


    }

    @Test
    public void createEmbeddedIndicesTest() {
        RawSchema schema = embeddedSchema;
        ElasticIndexProviderMappingFactory mappingFactory = new ElasticIndexProviderMappingFactory(client, schema, ontology, embeddedProvider);
        List<Tuple2<String, Boolean>> list = mappingFactory.generateMappings();
        Assert.assertEquals(list.size(),13);
        HashSet<String> labels = Sets.newHashSet("person", "horse", "dragon", "fire", "freeze",
                "kingdom", "guild", "own", "know", "memberof", "originatedin", "subjectof", "registeredin");

        Assert.assertEquals(list.stream().map(i->i._1).collect(Collectors.toSet()), labels);

        Iterable<String> allIndices = schema.indices();
        javaslang.collection.Stream.ofAll(allIndices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());

        Set<String> indices =  new HashSet<>(mappingFactory.createIndices());
        Set<String> names = new HashSet<>(Arrays.asList("idx_fire_500", "idx_freeze_2000", "idx_fire_1500", "idx_freeze_1000", "guilds", "own", "subjectof", "idx_freeze_1500", "people", "idx_fire_2000", "idx_fire_1000", "idx_freeze_500", "kingdoms", "know", "registeredin", "originatedin", "memberof", "horses", "dragons"));

        Assert.assertEquals(indices, names);
        indices.forEach(index -> {
            GetMappingsRequest request = new GetMappingsRequest();
            request.indices(index);
            GetMappingsResponse response = client.admin().indices().getMappings(request).actionGet();
            switch (index) {
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
                        Assert.assertEquals(map.get(index).toString(),"{mappings={Fire={properties={date={type=date, format=epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS}, direction={type=keyword}, entityA={properties={color={type=keyword}, id={type=keyword}, name={type=text, fields={keyword={type=keyword}}}, type={type=keyword}}}, entityB={properties={color={type=keyword}, id={type=keyword}, name={type=text, fields={keyword={type=keyword}}}, type={type=keyword}}}, id={type=keyword}, temperature={type=integer}, type={type=keyword}}}}}");
                    } catch (IOException e) {
                        Assert.fail("Not expecting non registered type " + index);
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
                        Assert.assertEquals(map.get(index).toString(),"{mappings={Freeze={properties={date={type=date, format=epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS}, direction={type=keyword}, entityA={properties={id={type=keyword}, name={type=text, fields={keyword={type=keyword}}}, type={type=keyword}}}, entityB={properties={id={type=keyword}, name={type=text, fields={keyword={type=keyword}}}, type={type=keyword}}}, id={type=keyword}, temperature={type=integer}, type={type=keyword}}}}}");
                    } catch (IOException e) {
                        Assert.fail("Not expecting non registered type " + index);
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
                case "Person":
                case "people":
                    Assert.assertEquals(response.toString(),"{\"people\":{\"mappings\":{\"Person\":{\"properties\":{\"birthDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"deathDate\":{\"type\":\"keyword\"},\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"gender\":{\"type\":\"keyword\"},\"height\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"lastName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"profession\":{\"properties\":{\"certification\":{\"type\":\"keyword\"},\"experience\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"salary\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "Horse":
                case "horses":
                    Assert.assertEquals(response.toString(),"{\"horses\":{\"mappings\":{\"Horse\":{\"properties\":{\"distance\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"maxSpeed\":{\"type\":\"integer\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"},\"weight\":{\"type\":\"integer\"}}}}}}");
                    break;
                case "Dragon":
                case "dragons":
                    Assert.assertEquals(response.toString(),"{\"dragons\":{\"mappings\":{\"Dragon\":{\"properties\":{\"birthDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"color\":{\"type\":\"keyword\"},\"gender\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"power\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "Kingdom":
                case "kingdoms":
                    Assert.assertEquals(response.toString(),"{\"kingdoms\":{\"mappings\":{\"Kingdom\":{\"properties\":{\"funds\":{\"type\":\"float\"},\"id\":{\"type\":\"keyword\"},\"independenceDay\":{\"type\":\"keyword\"},\"king\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"queen\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "Guilds":
                case "guilds":
                    Assert.assertEquals(response.toString(),"{\"guilds\":{\"mappings\":{\"Guild\":{\"properties\":{\"description\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"establishDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"iconId\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"},\"url\":{\"type\":\"keyword\"}}}}}}");
                    break;
                default:
                    Assert.fail("Not expecting non registered type " + index);
            }
        });


    }

    @Test
    public void createSingleIndicesTest() {
        RawSchema schema = singleIndexSchema;
        ElasticIndexProviderMappingFactory mappingFactory = new ElasticIndexProviderMappingFactory(client, schema, ontology, singleIndexProvider);
        List<Tuple2<String, Boolean>> list = mappingFactory.generateMappings();
        Assert.assertEquals(list.size(),13);
        HashSet<String> labels = Sets.newHashSet("person", "horse", "dragon", "fire", "freeze",
                "kingdom", "guild", "own", "know", "memberof", "originatedin", "subjectof", "registeredin");

        Assert.assertEquals(list.stream().map(i->i._1).collect(Collectors.toSet()), labels);

        Iterable<String> allIndices = schema.indices();
        javaslang.collection.Stream.ofAll(allIndices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());

        Set<String> indices =  new HashSet<>(mappingFactory.createIndices());
        Set<String> names = new HashSet<>(Arrays.asList("idx_fire_500", "idx_freeze_2000", "idx_fire_1500", "idx_freeze_1000", "guilds", "own", "subjectof", "idx_freeze_1500", "people", "idx_fire_2000", "idx_fire_1000", "idx_freeze_500", "kingdoms", "know", "registeredin", "originatedin", "memberof", "horses", "dragons"));

        Assert.assertEquals(indices, names);
        indices.forEach(index -> {
            GetMappingsRequest request = new GetMappingsRequest();
            request.indices(index);
            GetMappingsResponse response = client.admin().indices().getMappings(request).actionGet();
            switch (index) {
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
                        Assert.assertEquals(map.get(index).toString(),"{mappings={Fire={properties={date={type=date, format=epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS}, direction={type=keyword}, entityA={properties={color={type=keyword}, id={type=keyword}, name={type=text, fields={keyword={type=keyword}}}, type={type=keyword}}}, entityB={properties={color={type=keyword}, id={type=keyword}, name={type=text, fields={keyword={type=keyword}}}, type={type=keyword}}}, id={type=keyword}, temperature={type=integer}, type={type=keyword}}}}}");
                    } catch (IOException e) {
                        Assert.fail("Not expecting non registered type " + index);
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
                        Assert.assertEquals(map.get(index).toString(),"{mappings={Freeze={properties={date={type=date, format=epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS}, direction={type=keyword}, entityA={properties={id={type=keyword}, name={type=text, fields={keyword={type=keyword}}}, type={type=keyword}}}, entityB={properties={id={type=keyword}, name={type=text, fields={keyword={type=keyword}}}, type={type=keyword}}}, id={type=keyword}, temperature={type=integer}, type={type=keyword}}}}}");
                    } catch (IOException e) {
                        Assert.fail("Not expecting non registered type " + index);
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
                case "Person":
                case "people":
                    Assert.assertEquals(response.toString(),"{\"people\":{\"mappings\":{\"Person\":{\"properties\":{\"birthDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"deathDate\":{\"type\":\"keyword\"},\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"gender\":{\"type\":\"keyword\"},\"height\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"lastName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"profession\":{\"properties\":{\"certification\":{\"type\":\"keyword\"},\"experience\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"salary\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "Horse":
                case "horses":
                    Assert.assertEquals(response.toString(),"{\"horses\":{\"mappings\":{\"Horse\":{\"properties\":{\"distance\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"maxSpeed\":{\"type\":\"integer\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"},\"weight\":{\"type\":\"integer\"}}}}}}");
                    break;
                case "Dragon":
                case "dragons":
                    Assert.assertEquals(response.toString(),"{\"dragons\":{\"mappings\":{\"Dragon\":{\"properties\":{\"birthDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"color\":{\"type\":\"keyword\"},\"gender\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"power\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "Kingdom":
                case "kingdoms":
                    Assert.assertEquals(response.toString(),"{\"kingdoms\":{\"mappings\":{\"Kingdom\":{\"properties\":{\"funds\":{\"type\":\"float\"},\"id\":{\"type\":\"keyword\"},\"independenceDay\":{\"type\":\"keyword\"},\"king\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"queen\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "Guilds":
                case "guilds":
                    Assert.assertEquals(response.toString(),"{\"guilds\":{\"mappings\":{\"Guild\":{\"properties\":{\"description\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"establishDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"iconId\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"},\"url\":{\"type\":\"keyword\"}}}}}}");
                    break;
                default:
                    Assert.fail("Not expecting non registered type " + index);
            }
        });


    }
}
