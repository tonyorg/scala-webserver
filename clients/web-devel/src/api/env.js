const backendDefaultDomain = 'http://localhost:8080';
console.log(process.env.BACKEND_URL);
const backendDomain = process.env.BACKEND_URL ? process.env.BACKEND_URL : backendDefaultDomain;
export default backendDomain;