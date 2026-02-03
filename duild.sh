# 1. Build the application jar
mvn -DskipTests clean package

# 2. Log in to Docker Hub (you can use your email if that's what you use to sign in)
docker login --username nabeel.amd93@gmail.com

# 3. Build the Docker image (repository name uses your Docker Hub account)
docker build -t nabeel.amd93/auth:latest -f Dockerfile .

# 4. (Optional) Verify local image
docker images nabeel.amd93/auth

# 5. Push the image to Docker Hub
docker push nabeel.amd93/auth:latest

# 6. (Optional) Logout
docker logout