import { login, logout } from '@/api/auth'
import { setToken, removeToken } from '@/utils/auth'

const state = {
  token: localStorage.getItem('token') || '',
  username: localStorage.getItem('username') || ''
}

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token
  },
  SET_USERNAME(state, username) {
    state.username = username
  },
  CLEAR_USER(state) {
    state.token = ''
    state.username = ''
  }
}

const actions = {
  async login({ commit }, userInfo) {
    const res = await login(userInfo)
    const { token } = res.data
    commit('SET_TOKEN', token)
    commit('SET_USERNAME', userInfo.username)
    setToken(token)
    localStorage.setItem('username', userInfo.username)
    return res
  },
  async logout({ commit }) {
    try {
      await logout()
    } catch (e) {
      // ignore
    }
    commit('CLEAR_USER')
    removeToken()
    localStorage.removeItem('username')
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
