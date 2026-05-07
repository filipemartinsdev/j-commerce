package com.products.application.factory;

import com.products.application.dto.PagedResponse;
import com.products.application.exception.InvalidEntityMapperException;
import com.products.application.exception.NullResponsePageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class PagedResponseFactoryTests {
    @InjectMocks
    private PagedResponseFactory<DTOTest> pagedResponseFactory;

    private static record EntityTest (
        UUID id,
        String name,
        String password
    ){}

    public static record DTOTest (UUID id, String name){}

    @Test @DisplayName("Should create PagedResponse successfully")
    public void fromPageTestCase1(){
//        Given

        EntityTest entityTest1 = new EntityTest(UUID.randomUUID(), "test", "test");

        EntityTest entityTest2 = new EntityTest(UUID.randomUUID(), "test2", "test2");

        List<EntityTest> entityTestList = new ArrayList<>();
        entityTestList.add(entityTest1);
        entityTestList.add(entityTest2);

        DTOTest dtoTest1 = new DTOTest(entityTest1.id, entityTest1.name);
        DTOTest dtoTest2 = new DTOTest(entityTest2.id, entityTest2.name);

        List<DTOTest> dtoTestList = new ArrayList<>();
        dtoTestList.add(dtoTest1);
        dtoTestList.add(dtoTest2);

        Function<EntityTest, DTOTest> entityMapperTest = new Function<EntityTest, DTOTest>() {
            @Override
            public DTOTest apply(EntityTest entityTest) {
                return new DTOTest(entityTest.id, entityTest.name);
            }
        };

        Page<EntityTest> page = Mockito.mock(Page.class);

        Mockito.when(page.getContent())
                .thenReturn(entityTestList);
        Mockito.when(page.getTotalPages())
                .thenReturn(1);
        Mockito.when(page.getNumber())
                .thenReturn(0);
        Mockito.when(page.getTotalPages())
                .thenReturn(1);
        Mockito.when(page.getTotalElements())
                .thenReturn(2L);
        Mockito.when(page.isLast()).thenReturn(true);
        Mockito.when(page.getSize()).thenReturn(20);

        var expectedResponse = PagedResponse.<DTOTest>builder()
                .page(0)
                .size(20)
                .isLast(true)
                .totalElements(2L)
                .totalPages(1)
                .content(dtoTestList)
                .build();

//        When

        PagedResponse<DTOTest> response = pagedResponseFactory.fromPage(page, entityMapperTest);

//        Then

        assertEquals(expectedResponse, response);
    }

    @Test @DisplayName("Should throw NullResponsePageException if page is null")
    public void fromPageTestCase2(){
        assertThrows(NullResponsePageException.class, () -> {
            pagedResponseFactory.fromPage(null, (entity) -> null);
        });
    }

    @Test @DisplayName("Should throw InvalidEntityMapperException if entityMapper is null")
    public void fromPageTestCase3(){
        assertThrows(InvalidEntityMapperException.class, () -> {
            pagedResponseFactory.fromPage(Mockito.mock(Page.class), null);
        });
    }

    @Test @DisplayName("Should return empty PagedResponse if content is empty")
    public void fromPageTestCase4(){
        //        Given

        List<EntityTest> entityTestList = new ArrayList<>();

        Function<EntityTest, DTOTest> entityMapperTest = entityTest -> new DTOTest(entityTest.id, entityTest.name);

        Page<EntityTest> page = Mockito.mock(Page.class);

        Mockito.when(page.getContent())
                .thenReturn(entityTestList);
        Mockito.when(page.getSize()).thenReturn(20);

        var expectedResponse = PagedResponse.<DTOTest>builder()
                .page(0)
                .size(20)
                .isLast(true)
                .totalElements(0L)
                .totalPages(0)
                .content(new ArrayList<>())
                .build();

//        When

        PagedResponse<DTOTest> response = pagedResponseFactory.fromPage(page, entityMapperTest);

//        Then

        assertEquals(expectedResponse, response);
    }
}
