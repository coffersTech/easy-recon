from setuptools import setup, find_packages

with open('requirements.txt', 'r') as f:
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