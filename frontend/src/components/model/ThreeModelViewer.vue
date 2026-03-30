<template>
  <div ref="container" class="three-container">
    <div v-if="loading" class="loading-overlay">
        <a-spin size="large" :tip="loadingTip" />
    </div>
    <div v-if="error" class="error-overlay">
        {{ error }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { VRMLoaderPlugin } from '@pixiv/three-vrm'
import { MMDLoader } from 'three-stdlib'

const props = defineProps<{
  url: string;
  format?: string; // e.g. 'vrm', 'pmx', 'glb'
}>()

const container = ref<HTMLElement>()
const loading = ref(true)
const loadingTip = ref('正在加载模型资源...')
const error = ref('')

let scene: THREE.Scene
let camera: THREE.PerspectiveCamera
let renderer: THREE.WebGLRenderer
let controls: OrbitControls
let animationFrameId: number
let clock: THREE.Clock

// MMD specific
let mmdHelper: any

// VRM specific
let currentVrm: any

onMounted(() => {
  initThree()
  loadModel()
})

onUnmounted(() => {
  cleanup()
})

watch(() => props.url, () => {
  cleanup()
  initThree()
  loadModel()
})

const initThree = () => {
  if (!container.value) return

  const width = container.value.clientWidth
  const height = container.value.clientHeight

  // Scene
  scene = new THREE.Scene()
  scene.background = new THREE.Color(0xf0f2f5) // Light gray background
  
  // Grid
  const gridHelper = new THREE.GridHelper(20, 20, 0xcccccc, 0xeeeeee)
  scene.add(gridHelper)

  // Camera
  camera = new THREE.PerspectiveCamera(45, width / height, 0.1, 1000)
  camera.position.set(0, 1.5, 3)

  // Renderer
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setSize(width, height)
  renderer.setPixelRatio(window.devicePixelRatio)
  renderer.outputColorSpace = THREE.SRGBColorSpace
  container.value.appendChild(renderer.domElement)

  // Controls
  const orbitControls = new OrbitControls(camera, renderer.domElement)
  ;(orbitControls as any).enableDamping = true
  controls = orbitControls
  controls.target.set(0, 1.0, 0)
  controls.update()
  
  // Lights
  const light = new THREE.DirectionalLight(0xffffff, 1.0)
  light.position.set(2, 2, 5).normalize()
  scene.add(light)
  
  const hemiLight = new THREE.HemisphereLight(0xffffff, 0x444444, 1.0)
  hemiLight.position.set(0, 20, 0)
  scene.add(hemiLight)
  
  const ambient = new THREE.AmbientLight(0x404040, 0.5)
  scene.add(ambient)
  
  clock = new THREE.Clock()
  
  animate()
}

const fitCameraToObject = (object: THREE.Object3D) => {
    const box = new THREE.Box3().setFromObject(object)
    const center = box.getCenter(new THREE.Vector3())
    const size = box.getSize(new THREE.Vector3())
    
    const maxDim = Math.max(size.x, size.y, size.z)
    const fov = camera.fov * (Math.PI / 180)
    let cameraZ = Math.abs(maxDim / 2 / Math.tan(fov / 2))
    
    cameraZ *= 2.0 // zoom out a bit
    camera.position.set(center.x, center.y + size.y / 4, center.z + cameraZ)
    
    const minZ = box.min.z
    const cameraToFarEdge = (minZ < 0) ? -minZ + cameraZ : cameraZ - minZ
    
    camera.far = cameraToFarEdge * 5
    camera.updateProjectionMatrix()
    
    if (controls) {
        controls.target.set(center.x, center.y, center.z)
        controls.update()
    }
}

const loadModel = () => {
    loading.value = true
    error.value = ''
    
    // Determine format
    let ext = props.format
    if (!ext) {
        ext = props.url.split('.').pop()?.toLowerCase() || ''
    }
    // Handle ?query params
    if (ext.includes('?')) ext = ext.split('?')[0]

    if (ext === 'vrm') {
        loadVRM()
    } else if (ext === 'pmx' || ext === 'pmd') {
        loadPMX()
    } else if (ext === 'glb' || ext === 'gltf') {
        loadGLTF()
    } else {
        error.value = '当前格式暂不支持在线预览，请下载查看'
        loading.value = false
    }
}

const loadVRM = () => {
    const loader = new GLTFLoader()
    loader.register((parser: any) => new VRMLoaderPlugin(parser))
    
    loader.load(
        props.url,
        (gltf: any) => {
            const vrm = gltf.userData.vrm
            if(vrm) {
                scene.add(vrm.scene)
                currentVrm = vrm
                vrm.scene.rotation.y = Math.PI // Face forward
                fitCameraToObject(vrm.scene)
            } else {
                // fallback if vrm data missing
                scene.add(gltf.scene)
                fitCameraToObject(gltf.scene)
            }
            loading.value = false
        },
        (progress: any) => {
             loadingTip.value = `加载中: ${(progress.loaded / progress.total * 100).toFixed(0)}%`
        },
        (err: any) => {
            console.error(err)
            error.value = '模型预览加载失败，请下载查看'
            loading.value = false
        }
    )
}

const loadPMX = () => {
    // MMDLoader requires ammo.wasm usually for physics, skipping physics for simple preview or need to load ammo
    // Simple load
    const loader = new MMDLoader()
    
    loader.load(
        props.url,
        (mesh: any) => {
            scene.add(mesh)
            fitCameraToObject(mesh)
            loading.value = false
        },
        (progress: any) => {
             loadingTip.value = `加载中: ${(progress.loaded / progress.total * 100).toFixed(0)}%`
        },
        (err: any) => {
            console.error(err)
            error.value = '模型预览加载失败，请下载查看'
            loading.value = false
        }
    )
}

const loadGLTF = () => {
    const loader = new GLTFLoader()
    loader.load(
        props.url,
        (gltf: any) => {
             scene.add(gltf.scene)
             fitCameraToObject(gltf.scene)
             loading.value = false
        },
        undefined,
        (err: any) => {
            console.error(err)
            error.value = '模型预览加载失败，请下载查看'
            loading.value = false
        }
    )
}

const animate = () => {
    animationFrameId = requestAnimationFrame(animate)
    const delta = clock.getDelta()
    
    if (currentVrm) {
        currentVrm.update(delta)
    }
    
    renderer.render(scene, camera)
}

const cleanup = () => {
    if (animationFrameId) cancelAnimationFrame(animationFrameId)
    if (renderer) {
        renderer.dispose()
        if (container.value && renderer.domElement) {
             container.value.removeChild(renderer.domElement)
        }
    }
}

</script>

<style scoped>
.three-container {
    width: 100%;
    height: 100%;
    position: relative;
    background: #f0f2f5;
    overflow: hidden;
}
.loading-overlay, .error-overlay {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    z-index: 10;
     pointer-events: none;
}
.error-overlay {
    color: red;
    font-weight: bold;
}
</style>
