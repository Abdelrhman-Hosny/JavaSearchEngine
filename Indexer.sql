USE [master]
GO
/****** Object:  Database [Indexer]    Script Date: 5/19/2022 2:19:26 AM ******/
CREATE DATABASE [Indexer]
 CONTAINMENT = NONE
 ON  PRIMARY 
( NAME = N'Indexer', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL12.SQLEXPRESS\MSSQL\DATA\Indexer.mdf' , SIZE = 11264KB , MAXSIZE = UNLIMITED, FILEGROWTH = 1024KB )
 LOG ON 
( NAME = N'Indexer_log', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL12.SQLEXPRESS\MSSQL\DATA\Indexer_log.ldf' , SIZE = 57664KB , MAXSIZE = 2048GB , FILEGROWTH = 10%)
GO
ALTER DATABASE [Indexer] SET COMPATIBILITY_LEVEL = 120
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [Indexer].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [Indexer] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [Indexer] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [Indexer] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [Indexer] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [Indexer] SET ARITHABORT OFF 
GO
ALTER DATABASE [Indexer] SET AUTO_CLOSE OFF 
GO
ALTER DATABASE [Indexer] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [Indexer] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [Indexer] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [Indexer] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [Indexer] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [Indexer] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [Indexer] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [Indexer] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [Indexer] SET  DISABLE_BROKER 
GO
ALTER DATABASE [Indexer] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [Indexer] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [Indexer] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [Indexer] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [Indexer] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [Indexer] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [Indexer] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [Indexer] SET RECOVERY SIMPLE 
GO
ALTER DATABASE [Indexer] SET  MULTI_USER 
GO
ALTER DATABASE [Indexer] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [Indexer] SET DB_CHAINING OFF 
GO
ALTER DATABASE [Indexer] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [Indexer] SET TARGET_RECOVERY_TIME = 0 SECONDS 
GO
ALTER DATABASE [Indexer] SET DELAYED_DURABILITY = DISABLED 
GO
USE [Indexer]
GO
/****** Object:  Table [dbo].[DocumentsTable]    Script Date: 5/19/2022 2:19:27 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[DocumentsTable](
	[document_name] [varchar](200) NOT NULL,
	[title] [varchar](200) NULL,
	[snippet] [varchar](200) NULL,
 CONSTRAINT [PK_DocumentsTable] PRIMARY KEY CLUSTERED 
(
	[document_name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[IndexTable]    Script Date: 5/19/2022 2:19:27 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[IndexTable](
	[word] [varchar](50) NOT NULL,
	[document_name] [varchar](200) NOT NULL,
	[title] [varchar](50) NULL,
	[h1] [int] NULL,
	[h2] [int] NULL,
	[h3] [int] NULL,
	[h4] [int] NULL,
	[bold] [int] NULL,
	[text] [int] NULL,
	[normalized_tf] [float] NULL,
 CONSTRAINT [PK_Indexer] PRIMARY KEY CLUSTERED 
(
	[word] ASC,
	[document_name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Queries]    Script Date: 5/19/2022 2:19:27 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Queries](
	[query] [varchar](200) NOT NULL,
 CONSTRAINT [PK_Queries] PRIMARY KEY CLUSTERED 
(
	[query] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[RankTable]    Script Date: 5/19/2022 2:19:27 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[RankTable](
	[document_name] [varchar](400) NOT NULL,
	[page_rank] [float] NOT NULL,
 CONSTRAINT [PK_RankTable] PRIMARY KEY CLUSTERED 
(
	[document_name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  StoredProcedure [dbo].[AddIndex_Entry]    Script Date: 5/19/2022 2:19:27 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE PROCEDURE [dbo].[AddIndex_Entry]
	-- Add the parameters for the stored procedure here
	@inWord varchar(50) ,
	@inDocument_name varchar(200) ,
	@inTitle int = 0,
	@inH1 int = 0,
	@inH2 int = 0,
	@inH3 int = 0,
	@inH4 int = 0,
	@inBold int = 0,
	@inText int = 0,
	@inTf float = 0
AS	

BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for procedure here
	insert into IndexTable values (@inWord,@inDocument_name,@inTitle,@inH1,@inH2,@inH3,@inH4,@inBold,@inText,@inTf);

END
GO
/****** Object:  StoredProcedure [dbo].[addPageRank]    Script Date: 5/19/2022 2:19:27 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create procedure [dbo].[addPageRank] @document varchar(400), @rank FLOAT
as
insert into RankTable
values(@document,@rank)
GO
/****** Object:  StoredProcedure [dbo].[AddQuery]    Script Date: 5/19/2022 2:19:27 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE PROCEDURE [dbo].[AddQuery] 
	-- Add the parameters for the stored procedure here
	@query varchar(200)
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for procedure here
	INSERT INTO Queries VALUES (@query);
END
GO
/****** Object:  StoredProcedure [dbo].[DeleteURLEntries]    Script Date: 5/19/2022 2:19:27 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE PROCEDURE [dbo].[DeleteURLEntries]
	-- Add the parameters for the stored procedure here
	@inDocument_name varchar(200)
	
AS	
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for procedure here
	delete from IndexTable where document_name = @inDocument_name;

END
GO
/****** Object:  StoredProcedure [dbo].[getPageRank]    Script Date: 5/19/2022 2:19:27 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create procedure [dbo].[getPageRank] @document varchar(50)
as
select page_rank from RankTable
where document_name = @document
GO
/****** Object:  StoredProcedure [dbo].[getWordInfo]    Script Date: 5/19/2022 2:19:27 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create procedure [dbo].[getWordInfo] @word varchar(50) , @document varchar(200)
as
SELECT title,h1,h2,h3,h4,bold,text,normalized_tf as tf 
FROM  IndexTable 
where word = @word and document_name = @document

GO
/****** Object:  StoredProcedure [dbo].[Insert_Document]    Script Date: 5/19/2022 2:19:27 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE PROCEDURE [dbo].[Insert_Document]
	-- Add the parameters for the stored procedure here
	@in_docname varchar(200) , 
	@in_doctitle varchar(200) ,
	@in_docsnippet varchar(200)
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for procedure here
insert into DocumentsTable values (@in_docname,@in_doctitle,@in_docsnippet);
END
GO
/****** Object:  StoredProcedure [dbo].[numberOfDocumentsForWord]    Script Date: 5/19/2022 2:19:27 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create procedure [dbo].[numberOfDocumentsForWord] @word varchar(50)
as 
select count(distinct document_name) as total from IndexTable 
where word = @word
GO
/****** Object:  StoredProcedure [dbo].[Retrieve_Document]    Script Date: 5/19/2022 2:19:27 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE PROCEDURE [dbo].[Retrieve_Document]
	-- Add the parameters for the stored procedure here
	@inurl varchar(200)
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for procedure here
select title,snippet from DocumentsTable where document_name= @inurl;
END
GO
/****** Object:  StoredProcedure [dbo].[RetrieveQuery]    Script Date: 5/19/2022 2:19:27 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE PROCEDURE [dbo].[RetrieveQuery] 
	-- Add the parameters for the stored procedure here
	@inString varchar(200)
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for procedure here
	SELECT * FROM Queries WHERE query LIKE @inString
END
GO
/****** Object:  StoredProcedure [dbo].[totalNumberOfDocuments]    Script Date: 5/19/2022 2:19:27 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create procedure [dbo].[totalNumberOfDocuments]
as
select count( distinct document_name) as numberOfDocuments from IndexTable 
GO
USE [master]
GO
ALTER DATABASE [Indexer] SET  READ_WRITE 
GO
