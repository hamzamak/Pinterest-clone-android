import express  from "express";
import User from "../models/user.js";
import bcrypt from 'bcrypt'
import jwt from "jsonwebtoken";
import dotenv from "dotenv";
import  {v2 as cloudinary} from "cloudinary"
import auth from "../middleware/auth.js";

dotenv.config();
const router = express.Router();

cloudinary.config({
  cloud_name : process.env.CLOUDINARY_CLOUD_NAME,
  api_key : process.env.CLOUDINARY_CLOUD_API_KEY ,
  api_secret : process.env.CLOUDINARY_CLOUD_API_SECRET
})

router.post('/signup' , async (req , res ) => {
    const { username , password , email  } = req.body ;

   try {
    const isUserExist = await User.findOne({$or : [{ email } , {username}]})

    if(isUserExist){
        return res.status(500).json({message:"user already exists"})
    }
    else {
        const hashedPassword = await bcrypt.hash(password, 12) ;
 
        const user = await User.create({
            email , password : hashedPassword , username
        })

        const token = jwt.sign({
            id : user._id , 
            username : user.username,
            email : user.email,
            avatar : user.avatarUrl
        } , process.env.SECRET, {
            expiresIn : '24h'
        }) ;

        res.status(200).json(  token );   
    }

    
   } catch (error) {
    return res.status(400).json({message:"erreur"})
   }


})

router.post('/signin' , async (req , res ) => {
    const { password , email  } = req.body ;

   try {
    const existingUser = await User.findOne({ email })

    if(!existingUser){
        return res.status(500).json({message:"no such email exists"})
    }
     const isPasswordCorrect = await bcrypt.compare(password, existingUser.password) ;
     if (!isPasswordCorrect) return res.status(400).json({ message: "Invalid Credential !" });
 
       
        const token = jwt.sign(
         {
            id : existingUser._id ,
            username : existingUser.username,
            email : existingUser.email,
            avatar : existingUser.avatarUrl
         } ,
         process.env.SECRET,
         {
            expiresIn : '24h'
        }) ;

        res.status(200).json(token );   
    
   } catch (error) {
    return res.status(404).json({message:"erreur"})
   }


})


router.get('/:id' , async (req , res ) => {
    const { id  } = req.params ;

   try {
    const user = await User.findById(id).select("-password");

    res.status(200).json(user );   
    
   } catch (error) {
    return res.status(404).json({message:"erreur"})
   }


})


router.put('/update/profil' ,auth, async (req , res ) => {
    const { avatarUrl , base_64_avatar  } = req.body ;
    if (!req.userId) return res.status(401). json({ message: "User is not Authenticated" });
   try {
    if(avatarUrl) {
        console.log(avatarUrl)
        const user = await User.findByIdAndUpdate(req.userId , {avatarUrl}, { new: true }).select("-password");

         const token = jwt.sign(
         {
            id : user._id ,
            username : user.username,
            email : user.email,
            avatar : user.avatarUrl
         } ,
         process.env.SECRET,
         {
            expiresIn : '24h'
        }) ;

        res.status(200).json(token );   

    }
   else if(base_64_avatar) {
     console.log(base_64_avatar)
        req.setTimeout(2400000); 
        const mediaUrl = await cloudinary.uploader.upload(base_64_avatar); //upload the image to the cloudinary
        const user =    await User.findByIdAndUpdate(req.userId , {avatarUrl : mediaUrl.url}, { new: true }).select("-password");
        const token = jwt.sign(
            {
               id : user._id ,
               username : user.username,
               email : user.email,
               avatar : user.avatarUrl
            } ,
            process.env.SECRET,
            {
               expiresIn : '24h'
           }) ;
   
           res.status(200).json(token ); 
    }
    else  res.status(400).send({message:"no data receives to updaate the profil "} );   

    
   } catch (error) {
    console.log(error)
    return res.status(404).json({message:"erreur"})
   }


})



export default router ; 