package com.products.infra.web.admin;

import com.products.application.dto.Response;
import com.products.application.dto.admin.*;
import com.products.application.service.ProductService;
import com.products.application.service.ScrollSubrangeExtractor;
import com.products.model.entity.Product;
import com.products.model.entity.ProductCategory;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
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
    @PreAuthorize("hasAnyRole('STOCK_MANAGER', 'LOGISTICS', 'ADMIN')")
    public Window<AdminProductResponse> products(
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
    @PreAuthorize("hasAnyRole('STOCK_MANAGER', 'LOGISTICS', 'ADMIN')")
    public AdminProductResponse product(
            @Argument String id
    ){
        return productService.getProductById(id);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public AdminProductResponse createProduct(
            @Argument CreateProductRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var userId = UUID.fromString(jwt.getSubject());
        return productService.createProduct(request, userId);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('STOCK_MANAGER', 'ADMIN')")
    public AdminProductResponse updateProduct(
            @Argument String id,
            @Argument UpdateProductRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var userId = UUID.fromString(jwt.getSubject());
        return productService.updateProduct(id, request, userId);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Response deleteProduct(
            @Argument String id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var userId = UUID.fromString(jwt.getSubject());
        productService.deleteProduct(id, userId);

        return new Response(true, "Product deleted successfully");
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('STOCK_MANAGER', 'LOGISTICS', 'ADMIN')")
    public List<ProductCategory> productCategories(){
        return productService.getAllCategories();
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ProductCategory createProductCategory(
            @Argument CreateProductCategoryRequest request,
            @AuthenticationPrincipal Jwt jwt
    ){
        var userId = UUID.fromString(jwt.getSubject());
        return productService.createProductCategory(request, userId);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('STOCK_MANAGER', 'ADMIN')")
    public AdminProductResponse createSKU(
            @Argument String productId,
            @Argument CreateProductSKURequest request,
            @AuthenticationPrincipal Jwt jwt
    ){
        var userId = UUID.fromString(jwt.getSubject());
        return productService.createSKU(productId, request, userId);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('STOCK_MANAGER', 'ADMIN')")
    public AdminProductResponse updateSKU(
            @Argument String SKU,
            @Argument UpdateProductSKURequest request,
            @AuthenticationPrincipal Jwt jwt
    ){
        var userId = UUID.fromString(jwt.getSubject());
        return productService.updateSKU(SKU, request, userId);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('STOCK_MANAGER', 'ADMIN')")
    public AdminProductResponse deleteSKU(
            @Argument String SKU,
            @AuthenticationPrincipal Jwt jwt
    ){
        var userId = UUID.fromString(jwt.getSubject());
        return productService.deleteSKU(SKU, userId);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('STOCK_MANAGER', 'ADMIN')")
    public UploadImageResponse uploadProductImage(
            @Argument String productId,
            @Argument String contentType,
            @AuthenticationPrincipal Jwt jwt
    ){
        var userId = UUID.fromString(jwt.getSubject());
        return productService.uploadImage(productId, contentType, userId);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('STOCK_MANAGER', 'ADMIN')")
    public Response deleteProductImage(
            @Argument String productId,
            @Argument UUID imageId,
            @AuthenticationPrincipal Jwt jwt
    ){
        var userId = UUID.fromString(jwt.getSubject());
        productService.deleteImage(productId, imageId, userId);
        return new Response(true, "Image deleted successfully");
    }
}
