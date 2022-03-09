package mingu.bookreactive.ch1to7.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemTest {
    @Test
    public void itemBasicsShouldWork() {
        Item sampleItem = new Item("item1", "TV", "TV tray", 19.99);

        assertThat(sampleItem.getId()).isEqualTo("item1");
        assertThat(sampleItem.getName()).isEqualTo("TV");
        assertThat(sampleItem.getDescription()).isEqualTo("TV tray");
        assertThat(sampleItem.getPrice()).isEqualTo(19.99);

        assertThat(sampleItem.toString()).isEqualTo("Item{id='item1', name='TV', description='TV tray', price=19.99}");

        Item sampleItem2 = new Item("item1", "TV", "TV tray", 19.99);
        assertThat(sampleItem).isEqualTo(sampleItem2);
    }
}