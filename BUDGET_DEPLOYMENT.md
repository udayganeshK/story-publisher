# 💰 Budget-Friendly AWS Deployment Guide

## Option 1: Single EC2 Instance (~$10-15/month)

### Architecture:
```
Internet → EC2 Instance (Docker Compose) → Local PostgreSQL
```

### Monthly Cost:
- **EC2 t3.micro**: ~$8-10 (eligible for free tier!)
- **Elastic IP**: ~$0 (free when attached)
- **Storage (20GB)**: ~$2
- **Data Transfer**: ~$1-2
- **Total**: **~$11-14/month** 🎉

### Setup Steps:

1. **Launch EC2 Instance**
   ```bash
   # Create EC2 instance (t3.micro with Ubuntu 22.04)
   # Enable ports 22 (SSH), 80 (HTTP), 443 (HTTPS)
   ```

2. **Install Dependencies on EC2**
   ```bash
   # SSH into your EC2 instance
   ssh -i your-key.pem ubuntu@your-ec2-ip
   
   # Install Docker
   sudo apt update
   sudo apt install docker.io docker-compose -y
   sudo usermod -aG docker ubuntu
   
   # Install Git
   sudo apt install git -y
   ```

3. **Deploy Application**
   ```bash
   # Clone your repository
   git clone https://github.com/udayganeshK/story-publisher.git
   cd story-publisher
   
   # Create production environment file
   cat > .env.prod << EOF
   POSTGRES_DB=storypublisher
   POSTGRES_USER=storypublisher
   POSTGRES_PASSWORD=your-secure-password
   JWT_SECRET=StoryPublisherSecretKeyForJWTTokenGenerationAndValidation2025
   CORS_ORIGINS=http://your-ec2-ip:3000,https://your-domain.com
   NEXT_PUBLIC_API_URL=http://your-ec2-ip:8080/api
   EOF
   
   # Start the application
   docker-compose -f docker-compose.yml --env-file .env.prod up -d
   ```

## Option 2: Heroku Deployment (~$0-25/month)

### Free Tier Option:
- **Heroku Free Dynos**: $0 (with sleep mode)
- **Heroku Postgres Free**: $0 (limited storage)
- **Total**: **$0/month** 🆓

### Paid Option:
- **Heroku Basic Dynos**: $7 × 2 = $14
- **Heroku Postgres Basic**: $9
- **Total**: **~$23/month**

## Option 3: Railway/Vercel/PlanetScale (~$15-25/month)

### Modern Serverless Stack:
- **Vercel** (Frontend): $0-20
- **Railway** (Backend): $5-10
- **PlanetScale** (Database): $0-10
- **Total**: **~$5-40/month**

## Option 4: DigitalOcean Droplet (~$6-12/month)

### Ultra Budget Option:
- **DigitalOcean $6 Droplet**: $6
- **Managed PostgreSQL**: $15 (or local DB for $0)
- **Total**: **~$6-21/month**
