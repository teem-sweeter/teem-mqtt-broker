<template>
  <pre v-html="preSetColor(data)" id="preDom" style="padding:5px;background-color: rgba(166,168,173,0.24);overflow: scroll"></pre>
</template>

<script setup>
// 颜色映射关系
const colorMap = {
  string: '#ce9178',
  number: '#b5cea8',
  boolean: '#569cd6',
  null: '#b5cea8',
  key: '#ff0101',
}
const data = defineModel()
const props = defineProps({
  // 文字颜色
  colors: {
    type: Object,
    default: () => ({
      string: '#ce9178',
      number: '#b5cea8',
      boolean: '#569cd6',
      null: '#b5cea8',
      key: '#ff0101',
    }),
  },
  // 展示json的时候，步长
  step: {
    type: [Number, String],
    default: 4,
  },
})

// 添加样式标签
const addStyle = () => {
  const obj = {
    ...colorMap,
    ...props.colors,
  }
  // 如果有了 就不创建了
  if (document.querySelector('#pre-id')) return
  const style = document.createElement('style')
  style.id = 'pre-id'
  style.innerText = `
    .string{ color: ${obj['string']} };
    .number{ color: ${obj['number']} };
    .boolean{ color: ${obj['boolean']} };
    .null{ color: ${obj['null']} };
    .key{ color: ${obj['key']} };
  `
  document.head.appendChild(style)
}
addStyle()
// pre设置颜色
function preSetColor(data) {
  function syntaxHighlight(json) {
    if (typeof json != 'string') {
      json = JSON.stringify(json, undefined, 2)
    }
    json = json.replace(/&/g, '&').replace(/</g, '<').replace(/>/g, '>')
    return json.replace(
      /("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g,
      function (match) {
        var cls = 'number'
        if (/^"/.test(match)) {
          if (/:$/.test(match)) {
            cls = 'key'
          } else {
            cls = 'string'
          }
        } else if (/true|false/.test(match)) {
          cls = 'boolean'
        } else if (/null/.test(match)) {
          cls = 'null'
        }
        return '<span class="' + cls + '">' + match + '</span>'
      },
    )
  }
  const handler = JSON.stringify(data, null, +props.step)
  return syntaxHighlight(handler)
}
</script>

<style lang="scss" scoped>
pre {
  padding:5px;
  background-color: rgba(166,168,173,0.24);
  overflow: scroll;
  text-align: left;
}
</style>
