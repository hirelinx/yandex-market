package ru.yandex.practicum.market.service;

import lombok.AllArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Primary;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.mapper.ItemMapper;
import ru.yandex.practicum.market.model.Item;
import ru.yandex.practicum.market.repository.ItemRepository;
import ru.yandex.practicum.market.web.request.ItemCreateRequest;

import java.util.UUID;

@Primary
@Service
@AllArgsConstructor
public class ItemWriteServiceImpl implements ItemWriteService {
    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;
    private final FilesService filesService;
    @Setter
    private ItemReadService itemReadService;

    @Override
    public Mono<ItemDto> create(ItemCreateRequest request, FilePart image) {
        var extension = StringUtils.getFilenameExtension(image.filename());
        var newFileName = UUID.randomUUID() + (extension != null ? "." + extension : "");
        return filesService.upload(image, newFileName).then(Mono.defer(() -> {
            var item = new Item();
            item.setTitle(request.getTitle());
            item.setDescription(request.getDescription());
            item.setImgPath("files/" + newFileName);
            item.setPrice(request.getPrice());

            return itemRepository.save(item)
                    .map(it -> itemMapper.toDto(it, 0L));
        }))
                .doOnSuccess(result -> itemReadService.cacheEvictOrders())
                .doOnSuccess(result -> itemReadService.cacheEvictOrderById(result.id()));
    }
}
