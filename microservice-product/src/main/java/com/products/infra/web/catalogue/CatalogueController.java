package com.products.infra.web.catalogue;

import com.products.application.dto.catalogue.CatalogueSearchResponse;
import com.products.application.dto.catalogue.CategoryCatalogueResponse;
import com.products.application.dto.catalogue.ProductCatalogueResponse;
import com.products.application.service.CatalogueServiceImpl;
import com.products.application.service.ScrollSubrangeExtractor;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class CatalogueController {
    private final CatalogueServiceImpl catalogueService;
    private final ScrollSubrangeExtractor scrollSubrangeExtractor;

    public CatalogueController(CatalogueServiceImpl catalogueService, ScrollSubrangeExtractor scrollSubrangeExtractor) {
        this.catalogueService = catalogueService;
        this.scrollSubrangeExtractor = scrollSubrangeExtractor;
    }

    @QueryMapping
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public List<CategoryCatalogueResponse> categories(){
        return catalogueService.getAllCategories();
    }

    @QueryMapping
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public ProductCatalogueResponse catalogueProduct(@Argument String id){
        return catalogueService.getProductById(id);
    }

    @QueryMapping
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public Window<ProductCatalogueResponse> catalogue(
            @Argument Long categoryId,
            ScrollSubrange subrange
    ) {
        var position = scrollSubrangeExtractor.getPosition(subrange);
        var limit = scrollSubrangeExtractor.getLimit(subrange);

        return resolveCatalogueRequest(categoryId, position, limit);
    }

    private Window<ProductCatalogueResponse> resolveCatalogueRequest(
            Long categoryId,
            ScrollPosition position,
            Limit limit
    ){
        if (categoryId != null)
            return catalogueService.getAllProductsByCategory(categoryId, position, limit);
        else
            return catalogueService.getAllProducts(position, limit);
    }

    @QueryMapping
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public CatalogueSearchResponse catalogueSearch(
            @Argument String query,
            @Argument Long categoryId
    ){
        if (categoryId != null)
            return catalogueService.search(query, categoryId);
        else
            return catalogueService.search(query);
    }
}
