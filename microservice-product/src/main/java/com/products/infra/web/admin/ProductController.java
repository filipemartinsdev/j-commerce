package com.products.infra.web.admin;

import com.products.application.dto.Response;
import com.products.application.dto.admin.*;
import com.products.application.service.ProductService;
import com.products.application.service.ScrollSubrangeExtractor;
import com.products.domain.entity.Product;
import com.products.domain.entity.ProductCategory;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class ProductController {
    private final ScrollSubrangeExtractor scrollSubrangeExtractor;
    private final ProductService productService;

    public ProductController(ScrollSubrangeExtractor scrollSubrangeExtractor, ProductService productService) {
        this.scrollSubrangeExtractor = scrollSubrangeExtractor;
        this.productService = productService;
    }

    @QueryMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_STOCK_MANAGER', 'SCOPE_LOGISTICS', 'SCOPE_ADMIN')")
    public Window<Product> products(
            @Argument Long categoryId,
            ScrollSubrange scrollSubrange
    ){
        ScrollPosition position = scrollSubrangeExtractor.getPosition(scrollSubrange);
        Limit limit = scrollSubrangeExtractor.getLimit(scrollSubrange);

        if(categoryId != null)
            return productService.getAllProductsByCategory(categoryId, position, limit);
        else
            return productService.getAllProducts(position, limit);
    }

    @QueryMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_STOCK_MANAGER', 'SCOPE_LOGISTICS', 'SCOPE_ADMIN')")
    public Product product(
            @Argument String id
    ){
        return productService.getProductById(id);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN')")
    public Product createProduct(
            @Argument CreateProductRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var userId = UUID.fromString(jwt.getSubject());
        return productService.createProduct(request, userId);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_STOCK_MANAGER', 'SCOPE_ADMIN')")
    public Product updateProduct(
            @Argument String id,
            @Argument UpdateProductRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var userId = UUID.fromString(jwt.getSubject());
        return productService.updateProduct(id, request, userId);
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public Response deleteProduct(
            @Argument String id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var userId = UUID.fromString(jwt.getSubject());
        productService.deleteProduct(id, userId);

        return new Response(true, "Product deleted successfully");
    }

    @QueryMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_STOCK_MANAGER', 'SCOPE_LOGISTICS', 'SCOPE_ADMIN')")
    public List<ProductCategory> productCategories(){
        return productService.getAllCategories();
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ProductCategory createProductCategory(
            @Argument CreateProductCategoryRequest request,
            @AuthenticationPrincipal Jwt jwt
    ){
        var userId = UUID.fromString(jwt.getSubject());
        return productService.createProductCategory(request, userId);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_STOCK_MANAGER', 'SCOPE_ADMIN')")
    public Product createSKU(
            @Argument String productId,
            @Argument CreateProductSKURequest request,
            @AuthenticationPrincipal Jwt jwt
    ){
        var userId = UUID.fromString(jwt.getSubject());
        return productService.createSKU(productId, request, userId);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_STOCK_MANAGER', 'SCOPE_ADMIN')")
    public Product updateSKU(
            @Argument String SKU,
            @Argument UpdateProductSKURequest request,
            @AuthenticationPrincipal Jwt jwt
    ){
        var userId = UUID.fromString(jwt.getSubject());
        return productService.updateSKU(SKU, request, userId);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_STOCK_MANAGER', 'SCOPE_ADMIN')")
    public Product deleteSKU(
            @Argument String SKU,
            @AuthenticationPrincipal Jwt jwt
    ){
        var userId = UUID.fromString(jwt.getSubject());
        return productService.deleteSKU(SKU, userId);
    }
}
