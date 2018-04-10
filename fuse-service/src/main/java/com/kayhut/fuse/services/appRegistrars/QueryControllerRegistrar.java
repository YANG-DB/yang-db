package com.kayhut.fuse.services.appRegistrars;

import com.cedarsoftware.util.io.JsonWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.kayhut.fuse.model.execution.plan.descriptors.PlanWithCostDescriptor;
import com.kayhut.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.model.transport.PlanTraceOptions;
import com.kayhut.fuse.services.controllers.QueryController;
import org.jooby.Jooby;
import org.jooby.Results;
import org.jooby.Status;

import java.util.Optional;

import static org.jooby.Status.NOT_FOUND;
import static org.jooby.Status.OK;

public class QueryControllerRegistrar extends AppControllerRegistrarBase<QueryController> {
    //region Constructors
    public QueryControllerRegistrar() {
        super(QueryController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** get the query store info */
        app.use(appUrlSupplier.queryStoreUrl())
                .get(req -> Results.with(this.getController(app).getInfo(), Status.OK));

        /** create a query */
        app.use(appUrlSupplier.queryStoreUrl())
                .post(req -> {
                    CreateQueryRequest createQueryRequest = req.body(CreateQueryRequest.class);
                    req.set(CreateQueryRequest.class, createQueryRequest);
                    req.set(PlanTraceOptions.class, createQueryRequest.getPlanTraceOptions());

                    ContentResponse<QueryResourceInfo> response = createQueryRequest.getCreateCursorRequest() == null ?
                            this.getController(app).create(createQueryRequest) :
                            this.getController(app).createAndFetch(createQueryRequest);

                    return Results.with(response, response.status());
                });

        /** get the query info */
        app.use(appUrlSupplier.resourceUrl(":queryId"))
                .get(req -> {
                    ContentResponse response = this.getController(app).getInfo(req.param("queryId").value());
                    return Results.with(response, response.status());
                });

        /** delete a query */
        app.use(appUrlSupplier.resourceUrl(":queryId"))
                .delete(req -> {
                    ContentResponse response = this.getController(app).delete(req.param("queryId").value());
                    return Results.with(response, response.status());
                });

        /** get the query verbose plan */
        app.use(appUrlSupplier.resourceUrl(":queryId") + "/planVerbose")
                .get(req -> {
                    ContentResponse response = this.getController(app).planVerbose(req.param("queryId").value());
                    //temporary fix for json serialization of object graphs
                    return Results.with(new ObjectMapper().writeValueAsString(response.getData()), response.status())
                            .type("application/json");
                });
        /** get the print of the execution plan */
        app.use(appUrlSupplier.resourceUrl(":queryId") + "/plan/print")
                .get(req -> {
                    ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> response = this.getController(app).explain(req.param("queryId").value());
                    String print = PlanWithCostDescriptor.print(response.getData());
                    ContentResponse<String> compose = ContentResponse.Builder.<String>builder(OK, NOT_FOUND)
                            .data(Optional.of(print))
                            .compose();
                    return Results.with(JsonWriter.objectToJson(compose), response.status());
                });

        /** get the query v1*/
        app.use(appUrlSupplier.resourceUrl(":queryId") + "/v1")
                .get(req -> {
                    ContentResponse response = this.getController(app).getV1(req.param("queryId").value());
                    return Results.with(JsonWriter.objectToJson(response), response.status());
                });
        /** get the query v1 print*/
        app.use(appUrlSupplier.resourceUrl(":queryId") + "/v1/print")
                .get(req -> {
                    ContentResponse<Query> response = this.getController(app).getV1(req.param("queryId").value());
                    String print = QueryDescriptor.print(response.getData());
                    ContentResponse<String> compose = ContentResponse.Builder.<String>builder(OK, NOT_FOUND)
                            .data(Optional.of(print))
                            .compose();
                    return Results.with(JsonWriter.objectToJson(compose), response.status());
                });

        /** get the asg query */
        app.use(appUrlSupplier.resourceUrl(":queryId") + "/asg")
                .get(req -> {
                    ContentResponse<AsgQuery> response = this.getController(app).getAsg(req.param("queryId").value());
                    return Results.with(JsonWriter.objectToJson(response), response.status());
                });

        /** get the asg query print*/
        app.use(appUrlSupplier.resourceUrl(":queryId") + "/asg/print")
                .get(req -> {
                    ContentResponse<AsgQuery> response = this.getController(app).getAsg(req.param("queryId").value());
                    String print = AsgQueryDescriptor.print(response.getData());
                    ContentResponse<String> compose = ContentResponse.Builder.<String>builder(OK, NOT_FOUND)
                            .data(Optional.of(print))
                            .compose();
                    return Results.with(JsonWriter.objectToJson(compose), response.status());
                });

        /** get the query plan execution */
        app.use(appUrlSupplier.resourceUrl(":queryId") + "/plan")
                .get(req -> {
                    ContentResponse response = this.getController(app).explain(req.param("queryId").value());
                    //temporary fix for jason serialization of object graphs
                    return Results.with(JsonWriter.objectToJson(response), response.status());
                });
    }
    //endregion
}
