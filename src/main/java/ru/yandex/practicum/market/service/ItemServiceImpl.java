package ru.yandex.practicum.market.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.dto.ItemsAndPagingDto;
import ru.yandex.practicum.market.exception.EntityNotFoundException;
import ru.yandex.practicum.market.mapper.ItemMapper;
import ru.yandex.practicum.market.model.CountedItem;
import ru.yandex.practicum.market.model.Item;
import ru.yandex.practicum.market.repository.CartRepository;
import ru.yandex.practicum.market.repository.CountedItemRepository;
import ru.yandex.practicum.market.repository.ItemRepository;
import ru.yandex.practicum.market.web.request.ItemCreateRequest;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;
    private final FilesService filesService;
    private final CartService cartService;

    @Override
    @Transactional
    public ItemsAndPagingDto search(String search, Pageable realPaging) {
        var page = itemRepository.findAllByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, realPaging);

        var items = page.getContent();

        var countMap = getCountMap();

        var itemDtos = items.stream().map(it -> itemMapper.toDto(it, countMap.getOrDefault(it.getId(), 0L))).toList();

        return new ItemsAndPagingDto(itemDtos, page.hasPrevious(), page.hasNext());
    }

    @Override
    public ItemDto retrieveById(long id) {
        var optItem = itemRepository.findById(id);
        if (optItem.isEmpty()) {
            var itemCriteria = new Item();
            itemCriteria.setId(id);
            throw new EntityNotFoundException(itemCriteria);
        }

        var count = getCountMap().getOrDefault(optItem.get().getId(), 0L);

        return itemMapper.toDto(optItem.get(), count);
    }

    private Map<Long, Long> getCountMap() {
        var cart = cartService.retrieveCartEntity();
        return cart.getCountedItems().stream()
                .collect(Collectors.toMap(countedItem -> countedItem.getItem().getId(), CountedItem::getCount));
    }

    @Override
    public ItemDto create(ItemCreateRequest request, MultipartFile image) {
        var extension = StringUtils.getFilenameExtension(image.getOriginalFilename());
        var newFileName = UUID.randomUUID() + (extension != null ? "." + extension : "");
        filesService.upload(image, newFileName);

        var item = new Item();
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setImgPath("files/" + newFileName);
        item.setPrice(request.getPrice());
        itemRepository.save(item);
        return itemMapper.toDto(item, 0L);
    }
}
