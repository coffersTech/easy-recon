from setuptools import setup, find_packages
import os

# 获取当前文件所在目录的绝对路径
current_dir = os.path.dirname(os.path.abspath(__file__))
# 构建 requirements.txt 文件的绝对路径
requirements_file = os.path.join(current_dir, 'requirements.txt')

with open(requirements_file, 'r') as f:
    requirements = f.read().splitlines()

setup(
    name="easy-recon-sdk",
    version="1.0.0",
    packages=find_packages(),
    description="Easy Recon SDK for Python",
    author="Coffers Tech",
    author_email="contact@cofferstech.com",
    url="https://github.com/coffersTech/easy-recon",
    install_requires=requirements,
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
    ],
    python_requires='>=3.7',
)