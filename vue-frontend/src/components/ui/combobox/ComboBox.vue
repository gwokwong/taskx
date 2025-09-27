<template>
  <div
    :class="cn(
      'relative w-full cursor-default overflow-hidden rounded-lg bg-white text-left shadow-md transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-white focus-visible:ring-opacity-75 focus-visible:ring-offset-2 focus-visible:ring-offset-teal-300 sm:text-sm',
      props.class
    )"
  >
    <ComboboxInput
      :class="cn(
        'w-full border-none py-2 pl-3 pr-10 text-sm leading-5 text-gray-900 focus:ring-0',
        inputClass
      )"
      :display-value="(item) => (item as any)?.[displayField] || ''"
      @change="query = $event.target.value"
      :placeholder="placeholder"
    />
    <ComboboxButton class="absolute inset-y-0 right-0 flex items-center pr-2">
      <ChevronUpDownIcon class="h-5 w-5 text-gray-400" aria-hidden="true" />
    </ComboboxButton>
    <TransitionRoot
      leave="transition ease-in duration-100"
      leaveFrom="opacity-100"
      leaveTo="opacity-0"
      @after-leave="query = ''"
    >
      <ComboboxOptions class="absolute z-50 mt-1 max-h-60 w-full overflow-auto rounded-md bg-white py-1 text-base shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none sm:text-sm">
        <div
          v-if="filteredItems.length === 0 && query !== ''"
          class="relative cursor-default select-none py-2 px-4 text-gray-700"
        >
          {{ emptyText }}
        </div>

        <ComboboxOption
          v-for="item in filteredItems"
          :key="getItemKey(item)"
          :value="item"
          as="template"
          v-slot="{ selected, active }"
        >
          <li
            :class="[
              'relative cursor-default select-none py-2 pl-10 pr-4',
              active ? 'bg-blue-600 text-white' : 'text-gray-900'
            ]"
          >
            <span
              :class="[
                'block truncate',
                selected ? 'font-medium' : 'font-normal'
              ]"
            >
              {{ item[displayField] }}
            </span>
            <span
              v-if="selected"
              :class="[
                'absolute inset-y-0 left-0 flex items-center pl-3',
                active ? 'text-white' : 'text-blue-600'
              ]"
            >
              <CheckIcon class="h-5 w-5" aria-hidden="true" />
            </span>
          </li>
        </ComboboxOption>
      </ComboboxOptions>
    </TransitionRoot>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  Combobox,
  ComboboxInput,
  ComboboxButton,
  ComboboxOptions,
  ComboboxOption,
  TransitionRoot,
} from '@headlessui/vue'
import { CheckIcon, ChevronUpDownIcon } from '@heroicons/vue/20/solid'
import { cn } from '@/lib/utils'

interface Props {
  modelValue?: any
  items: any[]
  displayField?: string
  valueField?: string
  placeholder?: string
  emptyText?: string
  class?: string
  inputClass?: string
}

const props = withDefaults(defineProps<Props>(), {
  displayField: 'name',
  valueField: 'id',
  placeholder: '请选择...',
  emptyText: '没有找到匹配项'
})

const emit = defineEmits<{
  'update:modelValue': [value: any]
}>()

const query = ref('')

const filteredItems = computed(() => {
  if (query.value === '') {
    return props.items
  }
  return props.items.filter((item) =>
    item[props.displayField]
      .toLowerCase()
      .replace(/\s+/g, '')
      .includes(query.value.toLowerCase().replace(/\s+/g, ''))
  )
})

const getItemKey = (item: any) => {
  return item[props.valueField] || item.id || item
}

const selectedItem = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})
</script>