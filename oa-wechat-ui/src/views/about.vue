<template>
  <div class="container">
    <van-nav-bar
      title="关于我们"
    />
    <div class="detail-wrap">
      <div class="item">
        在此感谢尚硅谷开源精神
      </div>
    </div>

  </div>
</template>

<script>
export default {
  name: "process",

  data() {
    return {
    };
  },

  created(){
  },

  methods: {

  }
}
</script>

<style lang="scss" scoped>
.container {
  padding: 20px;
  .detail-wrap {
    .item {
      color: #838485;
    }
  }
}
</style>
