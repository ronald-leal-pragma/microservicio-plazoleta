package com.pragma.plazoleta.infrastructure.out.jpa.mapper;

import com.pragma.plazoleta.domain.model.OrderItemModel;
import com.pragma.plazoleta.domain.model.OrderModel;
import com.pragma.plazoleta.infrastructure.out.jpa.entity.OrderEntity;
import com.pragma.plazoleta.infrastructure.out.jpa.entity.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface IOrderEntityMapper {

    @Mapping(target = "items", ignore = true)
    OrderEntity toEntity(OrderModel orderModel);

    @Mapping(target = "items", source = "items")
    OrderModel toModel(OrderEntity orderEntity);

    @Mapping(target = "idPedido", source = "pedido.id")
    OrderItemModel toItemModel(OrderItemEntity orderItemEntity);

    List<OrderItemModel> toItemModelList(List<OrderItemEntity> orderItemEntities);

    @Mapping(target = "pedido", ignore = true)
    OrderItemEntity toItemEntity(OrderItemModel orderItemModel);
}
